
const data = {
  "India": {
    "Telangana": ["Hyderabad", "Warangal", "Nizamabad", "Karimnagar"],
    "Maharashtra": ["Mumbai", "Pune", "Nagpur", "Nashik"],
    "Karnataka": ["Bengaluru", "Mysore", "Mangalore"],
    "Tamil Nadu": ["Chennai", "Coimbatore", "Madurai"],
    "Uttar Pradesh": ["Lucknow", "Kanpur", "Varanasi"]
  },
  "United States": {
    "California": ["San Francisco", "Los Angeles", "San Diego", "Sacramento"],
    "New York": ["New York City", "Buffalo", "Rochester"],
    "Texas": ["Houston", "Dallas", "Austin"],
    "Florida": ["Miami", "Orlando", "Tampa"]
  },
  "United Kingdom": {
    "England": ["London", "Manchester", "Birmingham"],
    "Scotland": ["Edinburgh", "Glasgow"],
    "Wales": ["Cardiff", "Swansea"],
    "Northern Ireland": ["Belfast"]
  },
  "Canada": {
    "Ontario": ["Toronto", "Ottawa", "Hamilton"],
    "Quebec": ["Montreal", "Quebec City"],
    "British Columbia": ["Vancouver", "Victoria"],
    "Alberta": ["Calgary", "Edmonton"]
  },
  "Australia": {
    "New South Wales": ["Sydney", "Newcastle"],
    "Victoria": ["Melbourne", "Geelong"],
    "Queensland": ["Brisbane", "Gold Coast"]
  },
  "Germany": {
    "Bavaria": ["Munich", "Nuremberg"],
    "Berlin": ["Berlin"],
    "North Rhine-Westphalia": ["Cologne", "Düsseldorf"]
  },
  "France": {
    "Île-de-France": ["Paris"],
    "Provence-Alpes-Côte d'Azur": ["Marseille", "Nice"],
    "Auvergne-Rhône-Alpes": ["Lyon"]
  },
  "Japan": {
    "Tokyo": ["Tokyo"],
    "Osaka": ["Osaka"],
    "Kansai": ["Kyoto", "Nara"]
  },
  "China": {
    "Beijing": ["Beijing"],
    "Shanghai": ["Shanghai"],
    "Guangdong": ["Guangzhou", "Shenzhen"]
  },
  "Brazil": {
    "São Paulo": ["São Paulo"],
    "Rio de Janeiro": ["Rio de Janeiro"],
    "Minas Gerais": ["Belo Horizonte"]
  },
  "South Africa": {
    "Gauteng": ["Johannesburg", "Pretoria"],
    "Western Cape": ["Cape Town"],
    "KwaZulu-Natal": ["Durban"]
  }
};

const disposableDomains = ["tempmail.com","mailinator.com","10minutemail.com","yopmail.com","disposable.com"];


const altErrorId = {
  
  confirmPassword: 'confirmError'
};


const country = document.getElementById('country');
const state = document.getElementById('state');
const city = document.getElementById('city');
const inputs = Array.from(document.querySelectorAll('input,select,textarea'));


Object.keys(data).sort().forEach(c => {
  const opt = document.createElement('option'); opt.value = c; opt.textContent = c; country.appendChild(opt);
});


country.addEventListener('change', () => {
  state.innerHTML = '<option value="">-- Select State --</option>';
  city.innerHTML = '<option value="">-- Select City --</option>';
  const sel = country.value;
  if(!sel) { validateField(country); return; }
  Object.keys(data[sel]).forEach(s => {
    const opt = document.createElement('option'); opt.value = s; opt.textContent = s; state.appendChild(opt);
  });
  validateField(country);
});


state.addEventListener('change', () => {
  city.innerHTML = '<option value="">-- Select City --</option>';
  const selCountry = country.value; const selState = state.value;
  if(!selState) { validateField(state); return; }
  data[selCountry][selState].forEach(ct => {
    const opt = document.createElement('option'); opt.value = ct; opt.textContent = ct; city.appendChild(opt);
  });
  validateField(state);
});

city.addEventListener('change', () => validateField(city));


function getErrorElementFor(el) {
  if (!el || !el.id) return null;
  
  let err = document.getElementById(el.id + 'Error');
  
  if (!err && altErrorId[el.id]) {
    err = document.getElementById(altErrorId[el.id]);
  }
  return err;
}

function showError(el, msg){
  if(!el) return;
  el.classList.add('error');
  const err = getErrorElementFor(el);
  if(err){ err.textContent = msg; err.style.display = 'block'; }
}
function hideError(el){
  if(!el) return;
  el.classList.remove('error');
  const err = getErrorElementFor(el);
  if(err){ err.textContent = ''; err.style.display = 'none'; }
}

function validateField(el){
  const id = el.id;
  if(id === 'firstName'){
    if(!el.value.trim()) showError(el,'First name is required'); else hideError(el);
  }
  if(id === 'lastName'){
    if(!el.value.trim()) showError(el,'Last name is required'); else hideError(el);
  }
  if(id === 'email'){
    const v = el.value.trim();
    if(!v) return showError(el,'Email is required');
    const re = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
    if(!re.test(v)) return showError(el,'Enter a valid email');
    const dom = v.split('@')[1].toLowerCase();
    if(disposableDomains.includes(dom)) return showError(el,'Disposable emails are not allowed');
    hideError(el);
  }
  if(id === 'phone'){
    const v = el.value.trim();
    if(!v) return showError(el,'Phone number is required');
    const c = country.value;
    if(c){
      if(!/^\+\d{1,4}[\s-]?\d{6,14}$/.test(v)) return showError(el,'Phone must include a valid country code and digits (e.g. +91 9876543210)');
    } else {
      if(!/^\+?\d[\d\s-]{5,}$/.test(v)) return showError(el,'Enter a valid phone number');
    }
    hideError(el);
  }
  if(id === 'country'){
    if(!el.value) showError(el,'Country is required'); else hideError(el);
  }
  if(id === 'state'){
    if(!el.value) showError(el,'State is required'); else hideError(el);
  }
  if(id === 'city'){
    if(!el.value) showError(el,'City is required'); else hideError(el);
  }
  if(id === 'password'){
    const ok = checkPassword(el.value);
    if(ok.score < 2) return showError(el,'Password too weak'); else hideError(el);
  }
  if(id === 'confirmPassword'){
    const pass = document.getElementById('password').value;
    if(el.value !== pass) showError(el,'Confirm password must match'); else hideError(el);
  }
  if(id === 'terms'){
    if(!el.checked) {
      const errEl = document.getElementById('termsError');
      if(errEl){ errEl.style.display = 'block'; errEl.textContent = 'You must accept terms'; }
    } else {
      const errEl = document.getElementById('termsError');
      if(errEl){ errEl.style.display='none'; }
    }
  }

  
  const genders = document.getElementsByName('gender');
  let gChecked = false; for(const r of genders) if(r.checked) gChecked = true;
  if(!gChecked) {
    const ge = document.getElementById('genderError');
    if(ge){ ge.style.display='block'; ge.textContent='Please select a gender'; }
  } else {
    const ge = document.getElementById('genderError');
    if(ge){ ge.style.display='none'; }
  }

  updateSubmitState();
}


const meterBar = document.getElementById('meterBar');
const meterText = document.getElementById('strength');
function checkPassword(pass){
  let score=0;
  if(pass.length>=8) score++;
  if(/[A-Z]/.test(pass)) score++;
  if(/[0-9]/.test(pass)) score++;
  if(/[^A-Za-z0-9]/.test(pass)) score++;
  return {score};
}
function updateMeter(pass){
  const {score}=checkPassword(pass);
  const widths = ['0%','30%','60%','80%','100%'];
  const txt=['Very Weak','Weak','Medium','Strong','Very Strong'];
  meterBar.style.width = widths[score];
  meterText.textContent = txt[score];
  if(score<=1) meterBar.style.background = '#fb7185';
  else if(score==2) meterBar.style.background = '#f59e0b';
  else meterBar.style.background = '#10b981';
}


inputs.forEach(i => {
  i.addEventListener('input', ()=>validateField(i));
  i.addEventListener('change', ()=>validateField(i));
});
document.getElementById('password').addEventListener('input', (e)=>{ updateMeter(e.target.value); validateField(e.target); });
document.getElementById('confirmPassword').addEventListener('input', (e)=>validateField(e.target));


function updateSubmitState(){
  const requiredIds = ['firstName','lastName','email','phone','password','confirmPassword','country','state','city'];
  let ok=true;
  for(const id of requiredIds){
    const el = document.getElementById(id);
    if(!el) continue;
    if(!el.value || el.value.trim()==='') ok=false;
  }
 
  const genders = document.getElementsByName('gender'); let g=false; for(const r of genders) if(r.checked) g=true; if(!g) ok=false;
  
  if(!document.getElementById('terms').checked) ok=false;

  
  const anyErrorFromClass = Array.from(document.querySelectorAll('.error-msg')).some(el => el.style.display==='block');

  
  const anyErrorFromIds = Array.from(document.querySelectorAll('[id$="Error"]'))
    .some(el => (el.style.display !== 'none' && (el.innerText && el.innerText.trim().length>0)));

  
  const anyAltErrors = Object.values(altErrorId).some(eid => {
    const el = document.getElementById(eid);
    return el && (el.style.display !== 'none' && (el.innerText && el.innerText.trim().length>0));
  });

  const anyError = anyErrorFromClass || anyErrorFromIds || anyAltErrors;

  if(anyError) ok=false;
  document.getElementById('submitBtn').disabled = !ok;
}


document.getElementById('submitBtn').addEventListener('click', async ()=>{
  const btn = document.getElementById('submitBtn'); btn.disabled = true; document.getElementById('status').textContent = 'Submitting...';
  const payload = {
    firstName: document.getElementById('firstName').value.trim(),
    lastName: document.getElementById('lastName').value.trim(),
    email: document.getElementById('email').value.trim(),
    phone: document.getElementById('phone').value.trim(),
    age: document.getElementById('age').value.trim(),
    gender: Array.from(document.getElementsByName('gender')).find(r=>r.checked).value,
    address: document.getElementById('address').value.trim(),
    country: document.getElementById('country').value,
    state: document.getElementById('state').value,
    city: document.getElementById('city').value
  };
  console.log('Submitting payload', payload);


  await new Promise(r => setTimeout(r, 900));
  document.getElementById('status').textContent = '';
  document.getElementById('finalMessage').style.display = 'block';
  document.getElementById('finalMessage').innerHTML = '<div class="success-msg">Registration Successful! Your profile has been submitted successfully.</div>';

  
  setTimeout(()=>{ document.getElementById('regForm').reset(); updateMeter(''); updateSubmitState(); document.getElementById('finalMessage').style.display='none'; }, 1200);
});


inputs.forEach(i => {
  i.addEventListener('focus', ()=> {
    const err = getErrorElementFor(i); if(err) { err.style.display = 'none'; err.textContent=''; }
    i.classList.remove('error');
  });
});


updateSubmitState();
