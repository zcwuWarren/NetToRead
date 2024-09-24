document.addEventListener("DOMContentLoaded", function() {
    const booksPerPage = 6;  // 每頁顯示 6 本書
    let currentPage = 1;
    let totalBooks = [];

    // 從 localStorage 中提取 jwtToken
    const token = localStorage.getItem('jwtToken');

    if (token) {
        // 調用後端 API 獲取用戶按讚的書籍
        fetch('/api/userPage/myLike', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ token: token })
        })
            .then(response => response.json())
            .then(books => {
                totalBooks = books;  // 保存所有書籍
                renderBooks(currentPage);  // 初始加載當前頁面書籍
                renderPagination();  // 渲染分頁按鈕
            })
            .catch(error => console.error("Error fetching liked books:", error));
    } else {
        console.error("No token found in localStorage.");
        // 如果沒有 token，可能需要引導用戶登錄
        window.location.href = '/account.html';  // 跳轉到登入頁面
    }

    // 渲染當前頁面的書籍
    function renderBooks(page) {
        const containerCategory = document.getElementById('container-category');
        containerCategory.innerHTML = '';  // 清空之前的內容

        const start = (page - 1) * booksPerPage;
        const end = page * booksPerPage;
        const booksToShow = totalBooks.slice(start, end);  // 根據當前頁數提取書籍

        // 如果沒有書籍，顯示提示
        if (booksToShow.length === 0) {
            containerCategory.innerHTML = '<p>No liked books found.</p>';
            return;
        }

        // 渲染書籍
        booksToShow.forEach(book => {
            const bookDiv = document.createElement('div');
            bookDiv.classList.add('book-container');

            // 添加點擊事件跳轉到 bookDetail 頁面
            bookDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
            });

            bookDiv.innerHTML = `
                <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
                <div class="book-name">${book.bookName}</div>
            `;
            containerCategory.appendChild(bookDiv);
        });
    }

    // 渲染分頁按鈕
    function renderPagination() {
        const paginationContainer = document.getElementById('pagination');
        paginationContainer.innerHTML = '';  // 清空現有分頁按鈕

        const totalPages = Math.ceil(totalBooks.length / booksPerPage);  // 計算總頁數

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage;  // 當前頁面按鈕禁用

            button.addEventListener('click', function() {
                currentPage = i;  // 更新當前頁數
                renderBooks(currentPage);  // 重新渲染當前頁面的書籍
            });

            paginationContainer.appendChild(button);
        }
    }
});
