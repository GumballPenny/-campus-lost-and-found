'use strict';
let accessToken = null;
let expiresAt = 0;
const records = new Map();
const $ = selector => document.querySelector(selector);
const labels = {REPORTED:'Заявлена потеря',FOUND:'Предмет найден',RETURNED:'Возвращён владельцу'};

function safeBody(body) {
    if (body && typeof body === 'object' && 'accessToken' in body) {
        return {tokenType:body.tokenType, expiresIn:body.expiresIn, accessToken:'[скрыт]'};
    }
    return body;
}
function feedback(target, text, error=false, body=null) {
    target.replaceChildren();
    const box=document.createElement('div');box.className='feedback'+(error?' error':'');box.textContent=text;
    if(body?.correlationId) { const small=document.createElement('small');small.textContent='correlationId: '+body.correlationId;box.append(small); }
    target.append(box);
}
async function api(path, {method='GET',body,anonymous=false}={}) {
    const headers={'X-Correlation-Id':crypto.randomUUID()};
    if (body!==undefined) headers['Content-Type']='application/json';
    if (accessToken&&!anonymous) headers.Authorization='Bearer '+accessToken;
    const response=await fetch(path,{method,headers,body:body===undefined?undefined:JSON.stringify(body)});
    const text=await response.text(); let data;
    try {data=JSON.parse(text);} catch {data={message:text||'Empty response'};}
    $('#api-result').textContent=method+' '+path+'\nHTTP '+response.status+'\n'+JSON.stringify(safeBody(data),null,2);
    return {response,data};
}
function showError(target,result) {
    feedback(target,result.response.status+' · '+result.data.code+' · '+result.data.message,true,result.data);
}
function renderItems() {
    $('#items').replaceChildren();
    if (!records.size) {
        const empty=document.createElement('p');empty.className='empty';
        empty.textContent='Пока нет записей. Зарегистрируйте первую вещь.';
        $('#items').append(empty);return;
    }
    for(const item of records.values()) {
        const card=document.createElement('article');card.className='item';
        const info=document.createElement('div');
        const key=document.createElement('span');key.className='item-key';key.textContent=item.businessKey;
        const title=document.createElement('h3');title.textContent=item.title;
        const status=document.createElement('span');status.className='badge'+(item.status==='FOUND'?' found':'');status.textContent=item.status+' · '+(labels[item.status]||'');
        const id=document.createElement('p');id.className='item-id';id.textContent=item.id;
        info.append(key,title,status,id);
        const actions=document.createElement('div');actions.className='item-actions';
        const read=document.createElement('button');read.className='secondary';read.textContent='Прочитать';read.onclick=()=>readItem(item.id).catch(()=>feedback($('#feedback'),'Сервер недоступен.',true));
        const found=document.createElement('button');found.className='secondary';found.textContent='✓ Найдено · CUSTODIAN';found.onclick=()=>markFound(item.id).catch(()=>feedback($('#feedback'),'Сервер недоступен.',true));
        actions.append(read,found);card.append(info,actions);$('#items').append(card);
    }
}
async function readItem(id,anonymous=false) {
    const result=await api('/api/v1/lost-items/'+encodeURIComponent(id),{anonymous});
    if (!result.response.ok) {showError($('#feedback'),result);return;}
    records.set(result.data.id,result.data);$('#item-id').value=result.data.id;renderItems();
    feedback($('#feedback'),'200 · Запись прочитана из PostgreSQL. Статус: '+result.data.status);
}
async function markFound(id) {
    const result=await api('/api/v1/lost-items/'+encodeURIComponent(id)+'/found',{method:'POST'});
    if (!result.response.ok) {
        showError($('#feedback'),result);
        if(result.response.status===403) {
            const deniedResult=$('#api-result').textContent;
            const read=await api('/api/v1/lost-items/'+encodeURIComponent(id));
            $('#api-result').textContent=deniedResult;
            if(read.response.ok) {
                records.set(id,read.data);renderItems();
                feedback($('#feedback'),'403 · FORBIDDEN · Действие доступно CUSTODIAN. Статус остался '+read.data.status,true,result.data);
            }
        }
        return;
    }
    records.set(id,result.data);renderItems();feedback($('#feedback'),'200 · Находка подтверждена. Новый статус: '+result.data.status);
}
$('#auth-form').addEventListener('submit',async event=>{
    event.preventDefault();accessToken=null;
    const button=$('#auth-submit');button.disabled=true;
    try {
        const body={username:$('#username').value,password:$('#password').value};
        const result=await api('/api/v1/auth/login',{method:'POST',body});
        $('#password').value='';
        if(!result.response.ok) {showError($('#auth-error'),result);return;}
        accessToken=result.data.accessToken;expiresAt=Date.now()+result.data.expiresIn*1000;
        const me=await api('/api/me');
        if(!me.response.ok) {accessToken=null;showError($('#auth-error'),me);return;}
        $('#auth-card').classList.add('hidden');$('#session-card').classList.remove('hidden');
        $('#identity').textContent=me.data.username;
        $('#welcome').textContent='Здравствуйте, '+me.data.username;
        $('#roles').textContent='Права: '+me.data.authorities.join(', ');
        $('#auth-error').replaceChildren();feedback($('#feedback'),'Вход выполнен. Срок действия токена — 15 минут.');
    } catch {accessToken=null;feedback($('#auth-error'),'Не удалось связаться с сервером.',true);}
    finally {button.disabled=false;}
});
$('#logout').onclick=()=>{
    accessToken=null;expiresAt=0;
    $('#auth-card').classList.remove('hidden');$('#session-card').classList.add('hidden');
    $('#identity').textContent='Вы не вошли';$('#api-result').textContent='Вы вышли. Токен удалён из памяти.';
    feedback($('#feedback'),'Вы вышли. Следующий запрос к API вернёт 401.');
};
$('#create-form').addEventListener('submit',async event=>{
    event.preventDefault();
    try {
        const result=await api('/api/v1/lost-items',{method:'POST',body:{businessKey:$('#business-key').value,title:$('#item-title').value}});
        if(!result.response.ok) {showError($('#feedback'),result);return;}
        records.set(result.data.id,result.data);$('#item-id').value=result.data.id;renderItems();
        feedback($('#feedback'),'201 · Предмет зарегистрирован. Статус: '+result.data.status);
    } catch {feedback($('#feedback'),'Сервер недоступен.',true);}
});
$('#find-form').addEventListener('submit',event=>{event.preventDefault();readItem($('#item-id').value).catch(()=>feedback($('#feedback'),'Сервер недоступен.',true));});
$('#anonymous').onclick=()=>readItem($('#item-id').value||'00000000-0000-0000-0000-000000000000',true).catch(()=>feedback($('#feedback'),'Сервер недоступен.',true));
setInterval(()=>{
    if(!accessToken)return;
    const left=Math.max(0,Math.ceil((expiresAt-Date.now())/1000));
    $('#expires').textContent='Сессия: '+Math.floor(left/60)+':'+String(left%60).padStart(2,'0');
    if(!left)$('#logout').click();
},1000);
fetch('/actuator/health').then(r=>r.json()).then(b=>$('#health').textContent=b.status==='UP'?'Сервис доступен':'Сервис требует внимания').catch(()=>$('#health').textContent='Нет связи с сервером');
