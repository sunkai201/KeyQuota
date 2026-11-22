(function () {
  'use strict';
  let field, left, right, marker, cursor = 0, changed;
  function render(text) {
    if (!field) return;
    cursor = Math.min(cursor, text.length);
    const prefix = text.slice(0, cursor), suffix = text.slice(cursor);
    if (left.data === prefix && right.data === suffix) return;
    left.data = prefix;
    right.data = suffix;
    const box = field.getBoundingClientRect(), tip = marker.getBoundingClientRect();
    if (tip.right > box.right - 5) field.scrollLeft += tip.right - box.right + 6;
    if (tip.left < box.left + 5) field.scrollLeft -= box.left - tip.left + 6;
  }
  function replace(text, start, end, value) {
    cursor = start + value.length;
    const next = text.slice(0, start) + value + text.slice(end);
    render(next);
    return next;
  }
  function bind(onMove) {
    field = document.getElementById('draft');
    left = document.createTextNode('');
    right = document.createTextNode('');
    marker = document.createElement('span');
    marker.className = 'draft-caret';
    marker.setAttribute('aria-hidden','true');
    field.textContent = '';
    field.appendChild(left); field.appendChild(marker); field.appendChild(right);
    changed = onMove;
    field.setAttribute('tabindex','0');
    field.setAttribute('aria-label','Draft. Tap to move the caret. · 草稿，点按定位光标');
    function move(index) {
      const text = left.data + right.data;
      cursor = Math.max(0, Math.min(text.length,index));
      render(text);
      changed();
    }
    field.addEventListener('click', e => {
      if (!e.detail && !e.clientX) return;
      // Measure rendered character boundaries, including spaces and horizontal scroll.
      const x = e.clientX;
      const before = x < marker.getBoundingClientRect().left;
      const node = before ? left : right;
      const range = document.createRange();
      let low = 0, high = node.length;
      while (low < high) {
        const mid = Math.floor((low + high) / 2);
        range.setStart(node,mid); range.setEnd(node,mid+1);
        const r = range.getBoundingClientRect();
        if (x < (r.left + r.right) / 2) high = mid;
        else low = mid + 1;
      }
      move((before ? 0 : left.length) + low);
    });
    field.addEventListener('keydown', e => {
      const moves = {ArrowLeft:cursor-1,ArrowRight:cursor+1,Home:0,End:left.length+right.length};
      if (Object.prototype.hasOwnProperty.call(moves,e.key)) { e.preventDefault(); move(moves[e.key]); }
    });
  }
  window.KQDraft = {
    bind, render, replace,
    get cursor() { return cursor; },
    before(text) { return text.slice(0,cursor); },
    insert(text,value) { return replace(text,cursor,cursor,value); },
    clear(text) { return replace(text,0,text.length,''); },
    eraseWord(text) {
      const start = text.slice(0,cursor).lastIndexOf(' ') + 1;
      const space = text.indexOf(' ',cursor);
      return replace(text,start,space < 0 ? text.length : space,'');
    }
  };
})();
