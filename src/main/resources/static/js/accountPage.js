// accountPage.js
document.addEventListener("DOMContentLoaded", function() {
    const loginContainer = document.getElementById('container-a-login-1');
    const registerContainer = document.getElementById('container-a-login-2');
    const createAccountButton = document.getElementById('create-account');
    const loginSubmitButton = document.getElementById('submit-login');
    const registerSubmitButton = document.getElementById('submit-register');
    const experienceLogin = document.getElementById('experience-account');

    const previousPage = document.referrer || 'index.html';
    const token = localStorage.getItem('jwtToken');

    if (token && !isTokenExpired(token)) {
        window.location.href = '/profile.html';
    } else {
        document.body.style.display = "block";
    }

    function isTokenExpired(token) {
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return true;  // invalid token
        }

        const decodedPayload = JSON.parse(atob(tokenParts[1]));
        const expirationDate = new Date(decodedPayload.exp * 1000);
        const now = new Date();

        return expirationDate < now;
    }

    function switchToLogin() {
        loginContainer.style.display = 'block';
        registerContainer.style.display = 'none';
    }

    function switchToRegister() {
        loginContainer.style.display = 'none';
        registerContainer.style.display = 'block';
    }

    createAccountButton.addEventListener('click', switchToRegister);

    loginSubmitButton.addEventListener('click', async () => {
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;

        if (!email || !password) {
            alert("Please fill in both fields.");
            return;
        }

        const loginForm = { email, password };

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(loginForm)
            });

            const result = await response.json();

            if (response.ok) {
                localStorage.setItem('jwtToken', result.data.token);
                alert("Login successful!");
                window.location.href = previousPage;
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error("Login failed: ", error);
            alert("Login failed. Please try again.");
        }
    });

    registerSubmitButton.addEventListener('click', async () => {
        const userName = document.getElementById('register-username').value;
        const email = document.getElementById('register-email').value;
        const password = document.getElementById('register-password').value;

        if (!userName || !email || !password) {
            alert("Please fill in all fields.");
            return;
        }

        const registerForm = { userName, email, password };

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(registerForm)
            });

            const result = await response.json();

            if (!result.message) {
                if (result.password)
                    alert(result.password)
                if (result.email)
                    alert(result.email)
                return;
            }

            if (response.ok) {
                alert(result.message);
                switchToLogin();
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error("Registration failed: ", error);
            alert("Registration failed. Please try again.");
        }
    });

    experienceLogin.addEventListener('click', function (){
        document.getElementById("login-email").value = "experience@example.com";
        document.getElementById("login-password").value = "AreYouKiddingMe?123";
        document.getElementById("submit-login").click();
    })
});