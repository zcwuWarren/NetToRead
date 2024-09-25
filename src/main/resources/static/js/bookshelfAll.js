// // /* bookshelfAll.js */
// document.addEventListener("DOMContentLoaded", function () {
//     const likesButton = document.getElementById('switch-likes');
//     const collectsButton = document.getElementById('switch-collects');
//     const latestLikesContainer = document.getElementById('latestLikesContainer');
//
//     // 預設顯示 likes 書籍
//     loadBookshelf('/api/bookPage/latest-likes');
//
//     // 點擊 "Likes" 按鈕時，載入最新的 likes 書籍
//     likesButton.addEventListener('click', () => {
//         loadBookshelf('/api/bookPage/latest-likes');
//     });
//
//     // 點擊 "Collects" 按鈕時，載入最新的 collects 書籍
//     collectsButton.addEventListener('click', () => {
//         loadBookshelf('/api/bookPage/latest-collect');
//     });
//
//     // 加載書籍的函數
//     function loadBookshelf(apiUrl) {
//         fetch(apiUrl)
//             .then(response => response.json())
//             .then(books => {
//                 latestLikesContainer.innerHTML = ''; // 清空之前的內容
//
//                 books.forEach(book => {
//                     const bookDiv = document.createElement('div');
//                     bookDiv.classList.add('book-container');
//
//                     // 為每本書設定點擊事件，跳轉到 bookDetail 頁面
//                     bookDiv.addEventListener('click', function () {
//                         window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
//                     });
//
//                     bookDiv.innerHTML = `
//                         <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
//                         <div class="book-name">${book.bookName}</div>
//                     `;
//                     latestLikesContainer.appendChild(bookDiv);
//                 });
//             })
//             .catch(error => console.error("Error fetching books:", error));
//     }
// });

/*=================================*/
document.addEventListener("DOMContentLoaded", function () {
    const likesButton = document.getElementById('switch-likes');
    const collectsButton = document.getElementById('switch-collects');
    const latestLikesContainer = document.getElementById('latestLikesContainer');
    const paginationContainer = document.getElementById('pagination-bookshelf');
    const booksPerPage = 6;
    let currentPage = 1;
    let totalBooks = [];

    // 預設顯示 likes 書籍
    loadBookshelf('/api/bookPage/latest-likes');

    // 點擊 "Likes" 按鈕時，載入最新的 likes 書籍
    likesButton.addEventListener('click', () => {
        currentPage = 1;  // 每次切換重置到第一頁
        loadBookshelf('/api/bookPage/latest-likes');
    });

    // 點擊 "Collects" 按鈕時，載入最新的 collects 書籍
    collectsButton.addEventListener('click', () => {
        currentPage = 1;  // 每次切換重置到第一頁
        loadBookshelf('/api/bookPage/latest-collect');
    });

    // 加載書籍的函數
    function loadBookshelf(apiUrl) {
        fetch(apiUrl)
            .then(response => response.json())
            .then(books => {
                totalBooks = books;  // 保存所有書籍，用於分頁
                renderBooks(currentPage);  // 顯示當前頁的書籍
                renderPagination();  // 渲染分頁按鈕
            })
            .catch(error => console.error("Error fetching books:", error));
    }

    // 渲染當前頁的書籍
    function renderBooks(page) {
        latestLikesContainer.innerHTML = '';  // 清空之前的內容

        const start = (page - 1) * booksPerPage;
        const end = page * booksPerPage;
        const booksToShow = totalBooks.slice(start, end);

        // 如果沒有書籍，顯示提示
        if (booksToShow.length === 0) {
            latestLikesContainer.innerHTML = '<p>No books found.</p>';
            return;
        }

        // 渲染書籍
        booksToShow.forEach(book => {
            const bookDiv = document.createElement('div');
            bookDiv.classList.add('book-container');

            // 設置點擊事件跳轉到 bookDetail 頁面
            bookDiv.addEventListener('click', function () {
                window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
            });

            bookDiv.innerHTML = `
                <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
                <div class="book-name">${book.bookName}</div>
            `;
            latestLikesContainer.appendChild(bookDiv);
        });
    }

    // 渲染分頁按鈕
    function renderPagination() {
        paginationContainer.innerHTML = '';  // 清空現有分頁按鈕

        const totalPages = Math.ceil(totalBooks.length / booksPerPage);

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage;  // 當前頁按鈕禁用

            button.addEventListener('click', () => {
                currentPage = i;  // 更新當前頁
                renderBooks(currentPage);  // 渲染當前頁書籍
                renderPagination();  // 重新渲染分頁按鈕，正確更新按鈕狀態
            });

            paginationContainer.appendChild(button);
        }
    }
});
