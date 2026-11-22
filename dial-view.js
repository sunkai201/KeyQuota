(function () {
  'use strict';
  var ns = 'http://www.w3.org/2000/svg';
  var cx = 160, cy = 115, radius = 98;

  function node(parent, name, attrs) {
    var el = document.createElementNS(ns, name);
    Object.keys(attrs).forEach(function (key) { el.setAttribute(key, attrs[key]); });
    parent.appendChild(el);
    return el;
  }
  function point(degrees, r) {
    var a = degrees * Math.PI / 180;
    return (cx + Math.sin(a) * r) + ' ' + (cy - Math.cos(a) * r);
  }
  function arc(start, end) {
    return 'M' + point(start, radius) + ' A' + radius + ' ' + radius + ' 0 0 1 ' + point(end, radius);
  }

  function create(svg, cfg) {
    var colors = cfg.colors, style = cfg.style;
    node(svg, 'path', {d: arc(-cfg.arcHalf, cfg.arcHalf), fill: 'none', stroke: colors.tick, 'stroke-width': style.arcThickness});
    for (var d = -cfg.arcHalf; d <= cfg.arcHalf; d += style.tickStepDeg) {
      var major = d % style.tickMajorEveryDeg === 0;
      node(svg, 'path', {d: 'M' + point(d, radius - (major ? style.tickMajorLen : style.tickLen)) + ' L' + point(d, radius - 1), stroke: colors.tick, 'stroke-width': major ? 2 : 1.5});
    }
    var zone = node(svg, 'path', {fill: 'none', stroke: colors.red, 'stroke-width': style.redThickness});
    var fx = node(svg, 'g', {fill: colors.hit});
    var hand = node(svg, 'g', {'class':'dial-hand'});
    var needle = node(hand, 'path', {d:'M-8 0 L-4 -70.56 L0 -98 L4 -70.56 L8 0 Z', fill:colors.pointer});
    var hub = node(hand, 'rect', {x:-11, y:-11, width:22, height:22, fill:colors.red});
    node(hand, 'rect', {x:-7, y:-7, width:14, height:14, fill:colors.pointer});
    node(hand, 'rect', {x:-3, y:-3, width:6, height:6, fill:colors.red});
    var lastCenter, lastWidth;
    return {
      cx:cx, cy:cy, radius:radius,
      paint:function (state, particles, time) {
        if (state.center !== lastCenter || state.redWidth !== lastWidth) {
          var half = state.redWidth / 2;
          zone.setAttribute('d', arc(state.center - half, state.center + half));
          lastCenter = state.center;
          lastWidth = state.redWidth;
        }
        var hit = state.feedback === 'hit', miss = state.feedback === 'miss';
        var flash = hit && (time - state.flashT0) * 1000 < cfg.hitFx.flashWhiteMs;
        zone.setAttribute('stroke', hit ? (flash ? colors.flash : colors.hit) : colors.red);
        zone.setAttribute('opacity', miss ? cfg.missFx.dimAlpha : 1);
        hand.setAttribute('transform', 'translate(' + cx + ' ' + cy + ') rotate(' + state.angle + ')');
        needle.setAttribute('fill', miss ? colors.red : colors.pointer);
        hub.setAttribute('fill', hit ? colors.hit : colors.red);
        while (fx.childNodes.length > particles.length) fx.removeChild(fx.lastChild);
        particles.forEach(function (p, i) {
          var dot = fx.childNodes[i] || node(fx, 'rect', {});
          dot.setAttribute('x', p.x - p.size / 2); dot.setAttribute('y', p.y - p.size / 2);
          dot.setAttribute('width', p.size); dot.setAttribute('height', p.size);
          dot.setAttribute('opacity', Math.max(0, p.life / p.ttl));
        });
      }
    };
  }

  function press(button, handler) {
    var last = -Infinity;
    function activate(e) {
      if (button.disabled || e.isPrimary === false || (e.pointerType === 'mouse' && e.button !== 0) ||
          (e.touches && e.touches.length !== 1)) return;
      if (e.cancelable) e.preventDefault();
      last = performance.now();
      handler(e.timeStamp > 0 && Math.abs(e.timeStamp - last) < 60000 ? e.timeStamp : last);
    }
    // Some IME windows expose PointerEvent but only deliver a touch stream.
    // When both arrive, the matching touchstart belongs to the same press.
    var pendingTouch = false;
    button.addEventListener('pointerdown', function (e) {
      if (e.isPrimary === false) return;
      pendingTouch = e.pointerType === 'touch';
      activate(e);
    });
    button.addEventListener('touchstart', function (e) {
      if (pendingTouch) {
        pendingTouch = false;
        if (e.cancelable) e.preventDefault();
        return;
      }
      activate(e);
    }, {passive:false});
    function released() { pendingTouch = false; }
    button.addEventListener('pointerup', released);
    button.addEventListener('pointercancel', released);
    button.addEventListener('click', function (e) {
      if (!button.disabled && (e.detail === 0 || performance.now() - last >= 800)) handler(performance.now());
    });
  }
  // Android can suspend frame callbacks while an IME window is being attached.
  // A delayed timer advances the same loop, with exactly one winner per frame.
  function cancelFrame(ticket) {
    if (!ticket || ticket.done) return;
    ticket.done = true;
    clearTimeout(ticket.timer);
    if (ticket.raf != null && window.cancelAnimationFrame) window.cancelAnimationFrame(ticket.raf);
  }
  function frame(callback) {
    var ticket = {done:false, raf:null, timer:null};
    function run() {
      if (ticket.done) return;
      cancelFrame(ticket);
      callback(performance.now());
    }
    ticket.timer = setTimeout(run, 80);
    if (window.requestAnimationFrame) ticket.raf = window.requestAnimationFrame(run);
    return ticket;
  }
  function bindTouchRelease(button) {
    var contact = null;
    button.addEventListener('touchstart', function(e) {
      contact = !button.disabled && e.touches.length === 1 ? e.changedTouches[0] : null;
    }, {passive:true});
    button.addEventListener('touchmove', function(e) {
      var p = e.changedTouches[0];
      if (contact && (Math.abs(contact.clientX-p.clientX)>10 || Math.abs(contact.clientY-p.clientY)>10)) contact = null;
    }, {passive:true});
    button.addEventListener('touchcancel', function() { contact = null; });
    button.addEventListener('touchend', function(e) {
      var start = contact, end = e.changedTouches[0]; contact = null;
      if (!start || !end || start.identifier !== end.identifier || button.disabled || !e.cancelable ||
          Math.abs(start.clientX-end.clientX)>10 || Math.abs(start.clientY-end.clientY)>10 ||
          !button.contains(document.elementFromPoint(end.clientX,end.clientY))) return;
      e.preventDefault();
      button.click();
    }, {passive:false});
  }
  window.KQDial = {create:create, press:press, frame:frame, cancelFrame:cancelFrame, bindTouchRelease:bindTouchRelease};
}());
