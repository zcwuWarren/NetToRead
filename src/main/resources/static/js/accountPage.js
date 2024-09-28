// accountPage.js
document.addEventListener("DOMContentLoaded", function() {
    const switchLoginButton = document.getElementById('switch-login');
    const switchRegisterButton = document.getElementById('switch-register');
    const loginContainer = document.getElementById('a-login');
    const registerContainer = document.getElementById('a-register');

    const loginSubmitButton = document.getElementById('submit-login');
    const registerSubmitButton = document.getElementById('submit-register');

    // 取得上一頁的 URL，如果沒有上一頁，則預設為 'index.html'
    const previousPage = document.referrer || 'index.html';

    const token = localStorage.getItem('jwtToken');

    if (token && !isTokenExpired(token)) {
        // Token 存在且未過期，跳轉到 profile 頁面
        window.location.href = '/profile.html';
    } else {
        // 如果 token 過期或不存在，顯示頁面
        document.body.style.display = "block";
    }

    // 檢查 JWT Token 是否過期
    function isTokenExpired(token) {
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return true;  // 無效的 token
        }

        const decodedPayload = JSON.parse(atob(tokenParts[1]));
        const expirationDate = new Date(decodedPayload.exp * 1000);  // exp 是以秒計算的
        const now = new Date();

        return expirationDate < now;  // 如果當前時間大於過期時間，token 已過期
    }

    // 切換至 Login
    switchLoginButton.addEventListener('click', () => {
        loginContainer.style.display = 'block';
        registerContainer.style.display = 'none';
    });

    // 切換至 Register
    switchRegisterButton.addEventListener('click', () => {
        loginContainer.style.display = 'none';
        registerContainer.style.display = 'block';
    });

    // Login 提交
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
                localStorage.setItem('jwtToken', result.data.token); // 儲存 JWT token
                alert("Login successful!");
                window.location.href = previousPage; // 成功後跳轉至上一頁
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error("Login failed: ", error);
            alert("Login failed. Please try again.");
        }
    });

    // Register 提交
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

            if (response.ok) {
                alert(result.message);
                window.location.href = 'index.html'; // 註冊成功後跳轉至首頁
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error("Registration failed: ", error);
            alert("Registration failed. Please try again.");
        }
    });
});
