import React, { useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import './style.css'
import './hero-fix.css'

const initialClient = { nome:'', email:'', senha:'', rg:'', cpf:'', endereco:'', profissao:'', empregos:[] }
const today = new Date().toISOString().slice(0,10)

function App() {
  const [credentials, setCredentials] = useState(null)
  const [user, setUser] = useState(null)
  const [view, setView] = useState('pedidos')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [orders, setOrders] = useState([])
  const [cars, setCars] = useState([])
  const [clients, setClients] = useState([])
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(initialClient)
  const [orderForm, setOrderForm] = useState({automovelId:'',inicio:today,fim:today})
  const [editingOrder, setEditingOrder] = useState(null)
  const [carForm, setCarForm] = useState({matricula:'',ano:new Date().getFullYear(),marca:'',modelo:'',placa:''})
  const [register, setRegister] = useState(false)

  async function api(path, options={}, auth=credentials) {
    const headers = {'Content-Type':'application/json', ...options.headers}
    if (auth) headers.Authorization = `Basic ${btoa(unescape(encodeURIComponent(`${auth.email}:${auth.password}`)))}`
    const response = await fetch(`/api${path}`, {...options, headers})
    if (!response.ok) {
      let detail = ''
      try { const body = await response.json(); detail = body.detail || body.message || '' } catch {}
      throw new Error(response.status === 401 ? 'Usuário ou senha inválidos.' : detail || `Erro ${response.status}. Verifique os dados informados.`)
    }
    return response.status === 204 || response.status === 201 && (options.method === 'POST' && path.endsWith('/credito')) ? null : response.json()
  }
  async function run(action) {
    setError(''); setMessage(''); setBusy(true)
    try { await action() } catch (e) { setError(e.message) } finally { setBusy(false) }
  }
  async function refresh(current=user) {
    const [nextOrders,nextCars] = await Promise.all([api('/pedidos'),api('/automoveis')])
    setOrders(nextOrders); setCars(nextCars)
    if (current?.perfil === 'CLIENTE') setProfile(await api('/clientes/me'))
    else if (current) setClients(await api('/clientes'))
  }
  async function login(event) {
    event.preventDefault()
    await run(async () => {
      const auth = {email:form.email.trim(), password:form.senha}
      const result = await api('/auth/me',{},auth)
      setCredentials(auth); setUser(result); setView('pedidos'); setForm(initialClient)
    })
  }
  async function signup(event) {
    event.preventDefault()
    await run(async () => {
      const auth = {email:form.email.trim(), password:form.senha}
      await api('/auth/cadastro',{method:'POST',body:JSON.stringify({...form,email:auth.email})},null)
      const result = await api('/auth/me',{},auth)
      setCredentials(auth); setUser(result); setView('pedidos'); setForm(initialClient)
    })
  }
  useEffect(() => { if (credentials && user) refresh(user).catch(e => setError(e.message)) }, [credentials, user])
  function logout() { setCredentials(null); setUser(null); setProfile(null); setOrders([]); setCars([]); setClients([]); setError(''); setMessage('') }
  async function saveProfile(event) {
    event.preventDefault()
    await run(async () => { setProfile(await api('/clientes/me',{method:'PUT',body:JSON.stringify(profile)})); setMessage('Dados atualizados com sucesso.') })
  }
  async function deleteProfile() {
    if (!confirm('Excluir seu cadastro? Essa ação não poderá ser desfeita.')) return
    await run(async () => { await api('/clientes/me',{method:'DELETE'}); logout() })
  }
  async function saveOrder(event) {
    event.preventDefault()
    await run(async () => {
      await api(editingOrder ? `/pedidos/${editingOrder}` : '/pedidos', {method:editingOrder?'PUT':'POST',body:JSON.stringify({...orderForm,automovelId:Number(orderForm.automovelId)})})
      setEditingOrder(null); setOrderForm({automovelId:'',inicio:today,fim:today}); setMessage('Pedido salvo com sucesso.'); await refresh()
    })
  }
  async function cancelOrder(id) {
    if (!confirm('Cancelar este pedido?')) return
    await run(async () => { await api(`/pedidos/${id}/cancelamento`,{method:'POST'}); setMessage('Pedido cancelado.'); await refresh() })
  }
  async function evaluate(id,status) {
    const parecer = ['APROVADO','RECUSADO'].includes(status) ? prompt('Informe o parecer financeiro:') : ''
    if (['APROVADO','RECUSADO'].includes(status) && !parecer?.trim()) return
    await run(async () => { await api(`/pedidos/${id}/avaliacao`,{method:'PATCH',body:JSON.stringify({status,parecer})}); setMessage('Status atualizado.'); await refresh() })
  }
  async function credit(id) {
    const valor = prompt('Valor do crédito (R$):')
    if (!valor) return
    const parcelas = prompt('Número de parcelas:')
    if (!parcelas) return
    await run(async () => { await api(`/pedidos/${id}/credito`,{method:'POST',body:JSON.stringify({valor:Number(valor.replace(',','.')),parcelas:Number(parcelas)})}); setMessage('Contrato de crédito registrado.') })
  }
  async function saveCar(event) {
    event.preventDefault()
    await run(async () => { await api('/automoveis',{method:'POST',body:JSON.stringify(carForm)}); setCarForm({matricula:'',ano:new Date().getFullYear(),marca:'',modelo:'',placa:''}); setMessage('Automóvel cadastrado.'); await refresh() })
  }
  const client = user?.perfil === 'CLIENTE'
  return <div className="app">
    <header className="topbar"><div className="brand"><span className="brand-icon">A</span><div><strong>AlugaMais</strong><small>GESTÃO DE ALUGUÉIS</small></div></div>{user && <div className="user-menu"><span>{user.nome}<small>{client?'Cliente':user.perfil === 'BANCO'?'Agente bancário':'Agente empresa'}</small></span><button className="ghost" onClick={logout}>Sair</button></div>}</header>
    {!user ? <main className="auth-layout"><div className="hero"><span className="eyebrow">SISTEMA DE ALUGUEL DE CARROS</span><h1>Seu próximo caminho começa aqui.</h1><p>Cadastre automóveis, acompanhe pedidos e gerencie cada etapa do aluguel em um só lugar.</p><div className="hero-art"><div className="road"/><div className="car-shape" aria-hidden="true"><span className="car-roof"/><span className="car-body"/><span className="car-wheel car-wheel-left"/><span className="car-wheel car-wheel-right"/></div></div><div className="hero-bottom"><span>01 / 03</span><span>Simples. Transparente. Seguro.</span></div></div><section className="auth-card"><span className="pill">ACESSO AO SISTEMA</span><h2>{register?'Crie sua conta':'Bem-vindo de volta'}</h2><p>{register?'Preencha seus dados para começar.':'Entre com suas credenciais para continuar.'}</p><form onSubmit={register?signup:login}>
      {register && <><label>Nome completo<input required value={form.nome} onChange={e=>setForm({...form,nome:e.target.value})}/></label><div className="form-grid"><label>RG<input required value={form.rg} onChange={e=>setForm({...form,rg:e.target.value})}/></label><label>CPF (11 dígitos)<input required inputMode="numeric" pattern="[0-9]{11}" value={form.cpf} onChange={e=>setForm({...form,cpf:e.target.value})}/></label></div><label>Endereço<input required value={form.endereco} onChange={e=>setForm({...form,endereco:e.target.value})}/></label><label>Profissão<input required value={form.profissao} onChange={e=>setForm({...form,profissao:e.target.value})}/></label></>}
      <label>E-mail<input type="email" required value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label><label>Senha<input type="password" minLength={register?8:undefined} required value={form.senha} onChange={e=>setForm({...form,senha:e.target.value})}/></label>
      {error && <div className="alert error">{error}</div>}<button className="primary wide" disabled={busy}>{busy?'Aguarde...':register?'Criar conta':'Entrar no sistema'} <span>→</span></button></form><p className="auth-switch">{register?'Já tem uma conta?':'Ainda não tem uma conta?'} <button className="link" onClick={()=>{setRegister(!register);setError('')}}>{register?'Faça login':'Cadastre-se'}</button></p></section></main> : <div className="workspace"><aside className="sidebar"><span className="side-label">MENU PRINCIPAL</span><button className={view==='pedidos'?'active':''} onClick={()=>setView('pedidos')}>▣ <span>Pedidos</span></button><button className={view==='automoveis'?'active':''} onClick={()=>setView('automoveis')}>▰ <span>Automóveis</span></button><button className={view==='clientes'?'active':''} onClick={()=>setView('clientes')}>♧ <span>{client?'Meu cadastro':'Clientes'}</span></button><div className="sidebar-foot"><span className="avatar">{user.nome?.[0]?.toUpperCase()}</span><div><strong>{user.nome}</strong><small>{user.email}</small></div></div></aside><main className="content"><div className="page-head"><div><span className="eyebrow">PAINEL / {view.toUpperCase()}</span><h1>{view==='pedidos'?'Gestão de pedidos':view==='automoveis'?'Automóveis':'Clientes'}</h1><p>{view==='pedidos'?'Acompanhe solicitações e o andamento de cada aluguel.':view==='automoveis'?'Confira os veículos cadastrados na plataforma.':'Gerencie os dados de identificação e rendimentos.'}</p></div><div className="head-date">{new Date().toLocaleDateString('pt-BR',{day:'2-digit',month:'long',year:'numeric'})}</div></div>{error && <div className="alert error">{error}</div>}{message && <div className="alert success">{message}</div>}
      {view==='pedidos' && <><div className="stats"><div><span>TOTAL DE PEDIDOS</span><strong>{orders.length}</strong></div><div><span>EM ANDAMENTO</span><strong>{orders.filter(o=>['PENDENTE','EM_ANALISE','APROVADO'].includes(o.status)).length}</strong></div><div><span>CONTRATADOS</span><strong>{orders.filter(o=>o.status==='CONTRATADO').length}</strong></div></div>{client && <section className="panel"><div className="panel-title"><div><h2>{editingOrder?'Editar pedido':'Novo pedido de aluguel'}</h2><p>Escolha um automóvel e o período desejado.</p></div></div><form className="inline-form" onSubmit={saveOrder}><label>Automóvel<select required value={orderForm.automovelId} onChange={e=>setOrderForm({...orderForm,automovelId:e.target.value})}><option value="">Selecione um automóvel</option>{cars.map(c=><option key={c.id} value={c.id}>{c.marca} {c.modelo} · {c.placa}</option>)}</select></label><label>Retirada<input type="date" required min={today} value={orderForm.inicio} onChange={e=>setOrderForm({...orderForm,inicio:e.target.value})}/></label><label>Devolução<input type="date" required min={orderForm.inicio} value={orderForm.fim} onChange={e=>setOrderForm({...orderForm,fim:e.target.value})}/></label><button className="primary" disabled={busy || !cars.length}>{editingOrder?'Salvar':'Criar pedido'}</button>{editingOrder && <button type="button" className="ghost" onClick={()=>{setEditingOrder(null);setOrderForm({automovelId:'',inicio:today,fim:today})}}>Cancelar edição</button>}</form>{!cars.length && <p className="hint">Cadastre um automóvel antes de criar um pedido.</p>}</section>}<section className="panel"><div className="panel-title"><div><h2>{client?'Meus pedidos':'Pedidos recebidos'}</h2><p>{orders.length} registro(s) encontrado(s)</p></div><button className="ghost" onClick={()=>run(()=>refresh())}>Atualizar ↻</button></div>{orders.length ? <div className="table-wrap"><table><thead><tr><th>PEDIDO</th><th>{client?'AUTOMÓVEL':'CLIENTE'}</th><th>PERÍODO</th><th>STATUS</th><th>AÇÕES</th></tr></thead><tbody>{orders.map(o=><tr key={o.id}><td className="strong">#{String(o.id).padStart(4,'0')}</td><td>{client?o.automovel:o.clienteNome}<small>{!client&&o.automovel}</small></td><td>{o.inicio} → {o.fim}</td><td><span className={`status ${o.status.toLowerCase()}`}>{o.status.replace('_',' ')}</span>{o.parecer&&<small title={o.parecer}>Parecer: {o.parecer}</small>}</td><td className="actions">{client ? <>{o.status==='PENDENTE'&&<button onClick={()=>{setEditingOrder(o.id);setOrderForm({automovelId:o.automovelId,inicio:o.inicio,fim:o.fim});window.scrollTo({top:0,behavior:'smooth'})}}>Editar</button>}{['PENDENTE','EM_ANALISE'].includes(o.status)&&<button onClick={()=>cancelOrder(o.id)}>Cancelar</button>}</> : <>{['PENDENTE','EM_ANALISE'].includes(o.status)&&<><button onClick={()=>evaluate(o.id,'EM_ANALISE')}>Analisar</button><button onClick={()=>evaluate(o.id,'APROVADO')}>Aprovar</button><button onClick={()=>evaluate(o.id,'RECUSADO')}>Recusar</button></>}{o.status==='APROVADO'&&<button onClick={()=>evaluate(o.id,'CONTRATADO')}>Contratar</button>}{user.perfil==='BANCO'&&['APROVADO','CONTRATADO'].includes(o.status)&&<button onClick={()=>credit(o.id)}>Crédito</button>}</>}</td></tr>)}</tbody></table></div> : <div className="empty"><span>▣</span><strong>Nenhum pedido por aqui</strong><p>Os pedidos criados aparecerão nesta lista.</p></div>}</section></>}
      {view==='automoveis' && <><section className="panel"><div className="panel-title"><div><h2>Cadastrar automóvel</h2><p>Veículos podem pertencer a clientes ou agentes.</p></div></div><form className="car-form" onSubmit={saveCar}>{[['matricula','Matrícula'],['ano','Ano'],['marca','Marca'],['modelo','Modelo'],['placa','Placa']].map(([key,label])=><label key={key}>{label}<input required type={key==='ano'?'number':'text'} value={carForm[key]} onChange={e=>setCarForm({...carForm,[key]:key==='ano'?Number(e.target.value):e.target.value})}/></label>)}<button className="primary" disabled={busy}>Cadastrar</button></form></section><section className="panel"><div className="panel-title"><div><h2>Veículos cadastrados</h2><p>{cars.length} automóvel(is) disponível(is) para solicitação</p></div></div><div className="car-grid">{cars.map(c=><div className="car-card" key={c.id}><div className="car-visual">▰</div><span className="pill">{c.ano}</span><h3>{c.marca} {c.modelo}</h3><p>Placa {c.placa} · Matrícula {c.matricula}</p></div>)}</div>{!cars.length&&<div className="empty">Nenhum automóvel cadastrado.</div>}</section></>}
      {view==='clientes' && (client ? profile && <section className="panel profile-panel"><div className="panel-title"><div><h2>Meu cadastro</h2><p>Atualize seus dados e vínculos profissionais.</p></div></div><form onSubmit={saveProfile} className="profile-form">{[['nome','Nome completo'],['email','E-mail'],['rg','RG'],['cpf','CPF'],['endereco','Endereço'],['profissao','Profissão']].map(([key,label])=><label key={key}>{label}<input required disabled={key==='email'} value={profile[key]||''} onChange={e=>setProfile({...profile,[key]:e.target.value})}/></label>)}<div className="employment"><h3>Empregadores e rendimentos</h3>{profile.empregos.map((emp,i)=><div className="employment-row" key={i}><input placeholder="Empregador" value={emp.empregador} onChange={e=>setProfile({...profile,empregos:profile.empregos.map((item,j)=>j===i?{...item,empregador:e.target.value}:item)})}/><input type="number" min="0" step="0.01" placeholder="Rendimento mensal" value={emp.rendimento} onChange={e=>setProfile({...profile,empregos:profile.empregos.map((item,j)=>j===i?{...item,rendimento:Number(e.target.value)}:item)})}/><button type="button" className="ghost" onClick={()=>setProfile({...profile,empregos:profile.empregos.filter((_,j)=>j!==i)})}>Remover</button></div>)}{profile.empregos.length<3&&<button type="button" className="ghost" onClick={()=>setProfile({...profile,empregos:[...profile.empregos,{empregador:'',rendimento:0}]})}>+ Adicionar empregador</button>}</div><div className="profile-actions"><button className="primary" disabled={busy}>Salvar alterações</button><button type="button" className="danger" onClick={deleteProfile}>Excluir cadastro</button></div></form></section> : <section className="panel"><div className="panel-title"><div><h2>Clientes cadastrados</h2><p>{clients.length} cliente(s) na plataforma</p></div></div><div className="table-wrap"><table><thead><tr><th>NOME</th><th>CPF</th><th>PROFISSÃO</th><th>RENDIMENTOS</th></tr></thead><tbody>{clients.map(c=><tr key={c.id}><td className="strong">{c.nome}<small>{c.email}</small></td><td>{c.cpf}</td><td>{c.profissao}</td><td>{c.empregos.length} vínculo(s)</td></tr>)}</tbody></table></div>{!clients.length&&<div className="empty">Nenhum cliente cadastrado.</div>}</section>)}
    </main></div>}
    <footer>AlugaMais · Laboratório 02 · Sistema de Aluguel de Carros</footer>
  </div>
}

createRoot(document.getElementById('root')).render(<App />)
