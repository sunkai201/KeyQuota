(function(){
  'use strict';
  const native=(op,data='')=>window.prompt('KQ/'+op,data)||'';
  const wallet=()=>{try{const w=JSON.parse(native('wallet'));if(w && w.keys && typeof w.keys==='object')return w}catch(e){}return {keys:{}}};
  let balances=null, lastTicket='', seq=1;
  let timer;
  function notice(text){
    let node=document.getElementById('bridgeStatus');
    if(!node){node=document.createElement('div');node.id='bridgeStatus';node.setAttribute('role','status');document.body.appendChild(node)}
    node.textContent=text;node.hidden=false;clearTimeout(timer);timer=setTimeout(()=>node.hidden=true,2200);
  }
  window.KQHost={
    init(keys){
      const value=wallet();for(const k of Object.keys(keys))if(Number.isSafeInteger(value.keys[k]))keys[k]=value.keys[k];
      balances=Object.assign({},keys);lastTicket=JSON.stringify(value.ticket||null);
      return value.ticket||null;
    },
    save(keys,ticket){
      if(!balances)return;
      const delta={};let changed=false;
      for(const key in keys)if(keys[key]!==balances[key]){delta[key]=keys[key]-balances[key];changed=true}
      const stamp=JSON.stringify(ticket||null);
      if((changed || stamp!==lastTicket) && native('save',JSON.stringify({delta,ticket:ticket||null}))==='1'){
        balances=Object.assign({},keys);lastTicket=stamp;
      }
    },
    deliver(text){
      if(!text.trim())return false;
      const ok=native('deliver',JSON.stringify({seq,text}))==='1';
      if(ok){seq++;notice('Inserted. Tap Send in the app. · 已填入，请点应用的发送键。');}else notice('Return to the original text field and retry · 返回原输入框重试');
      return ok;
    },
    copy(text){return native('copy',text)==='1'},
    notice
  };
  if(native('private')==='1')document.documentElement.classList.add('private');
  document.addEventListener('click',e=>{
    const a=e.target.closest && e.target.closest('a[href^="https://"]');
    if(!a)return;e.preventDefault();
    if(native('link',a.href)!=='1')notice('No browser available · 无法打开浏览器');
  },true);
  if(!Element.prototype.replaceChildren)Element.prototype.replaceChildren=function(){this.textContent='';this.append.apply(this,arguments)};
  if(!Object.fromEntries)Object.fromEntries=xs=>{const out={};for(const pair of xs)out[pair[0]]=pair[1];return out};
  if(!Object.values)Object.values=obj=>Object.keys(obj).map(key=>obj[key]);
  if(!Object.entries)Object.entries=obj=>Object.keys(obj).map(key=>[key,obj[key]]);
  if(!String.prototype.padStart)String.prototype.padStart=function(len,fill){let v=String(this);fill=String(fill||' ');while(v.length<len)v=fill+v;return v};
  if(!NodeList.prototype.forEach)NodeList.prototype.forEach=Array.prototype.forEach;
  function prepareDialogs(){
    document.querySelectorAll('dialog').forEach(d=>{
      if(typeof d.showModal==='function')return;
      if(!('open' in d))Object.defineProperty(d,'open',{get(){return this.hasAttribute('open')}});
      const shade=document.createElement('div');
      shade.className='dialog-shade';
      d.classList.add('legacy-dialog');
      d.showModal=function(){
        if(this.open)return;
        this.parentNode.insertBefore(shade,this);this.setAttribute('open','');
      };
      d.close=function(){
        this.removeAttribute('open');
        if(shade.parentNode)shade.parentNode.removeChild(shade);
      };
    });
  }
  window.KQHost.prepareDialogs=prepareDialogs;
  document.addEventListener('DOMContentLoaded',prepareDialogs);
})();
