// Auth JS

function toggleForms() {
    const loginFormDiv = document.getElementById('loginFormDiv');
    const registerFormDiv = document.getElementById('registerFormDiv');
    
    loginFormDiv.style.display = loginFormDiv.style.display === 'none' ? 'block' : 'none';
    registerFormDiv.style.display = registerFormDiv.style.display === 'none' ? 'block' : 'none';
}

async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: username,
                password: password
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            const token = data.token;
            
            console.log('Login successful, token received:', token);
            
            // Extract role and student ID from JWT token
            try {
                const parts = token.split('.');
                if (parts.length === 3) {
                    const payload = JSON.parse(atob(parts[1]));
                    const role = payload.role || '';
                    localStorage.setItem('role', role);
                    console.log('Role extracted and stored:', role);
                    
                    // For students, we'll fetch their ID to enable enrollment
                    if (role.includes('STUDENT')) {
                        // We'll fetch student ID when needed
                        localStorage.setItem('isStudent', 'true');
                    }
                }
            } catch (e) {
                console.error('Failed to extract role:', e);
            }
            
            localStorage.setItem('token', token);
            localStorage.setItem('username', username);
            console.log('Token and username stored in localStorage');
            console.log('localStorage token after set:', localStorage.getItem('token'));
            
            errorDiv.textContent = 'Login successful! Redirecting...';
            errorDiv.style.color = 'green';
            
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1000);
        } else {
            errorDiv.textContent = 'Invalid credentials';
            errorDiv.style.color = 'red';
        }
    } catch (error) {
        console.error('Error:', error);
        errorDiv.textContent = 'Login failed. Please try again.';
        errorDiv.style.color = 'red';
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;
    const role = document.getElementById('regRole').value;
    const errorDiv = document.getElementById('registerError');
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: username,
                password: password,
                role: role
            })
        });
        
        if (response.ok) {
            errorDiv.textContent = 'Registration successful! Please login.';
            errorDiv.style.color = 'green';
            
            const form = document.getElementById('registerForm');
            console.log('Register form element:', form);
            console.log('Form type:', form.tagName);
            if (form && typeof form.reset === 'function') {
                form.reset();
            } else {
                console.error('Form not found or reset is not a function');
            }
            
            setTimeout(() => {
                toggleForms();
                errorDiv.textContent = '';
            }, 2000);
        } else {
            errorDiv.textContent = 'Registration failed. Please try again.';
            errorDiv.style.color = 'red';
        }
    } catch (error) {
        console.error('Error:', error);
        errorDiv.textContent = 'Registration failed. Please try again.';
        errorDiv.style.color = 'red';
    }
}

window.addEventListener('load', () => {
    const token = localStorage.getItem('token');
    if (token) {
        window.location.href = '/dashboard';
    }
});
