// /* bookshelfBySubCategory.js */
// /*======= 以 requestParameter 改寫，並加上 switch 按鈕============*/
// document.addEventListener("DOMContentLoaded", function () {
//     // 提取 URL 中的 subCategory
//     const urlParams = new URLSearchParams(window.location.search);
//     const subCategory = urlParams.get('subCategory');
//
//     const likesButton = document.getElementById('switch-likes');
//     const collectsButton = document.getElementById('switch-collects');
//     const containerCategory = document.getElementById('container-category');
//     const paginationContainer = document.getElementById('pagination-bookshelf');
//     const booksPerPage = 6;
//     let currentPage = 1;
//     let totalBooks = [];
//
//     if (!subCategory) {
//         console.error("Missing subCategory in URL.");
//         return;
//     }
//
//     // 預設加載 Likes 書籍
//     loadBookshelf(`/api/bookPage/latest-likes-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`);
//
//     // 點擊 "Likes" 按鈕時，載入最新的 Likes 書籍
//     likesButton.addEventListener('click', () => {
//         console.log("Loading latest likes by subCategory...");
//         loadBookshelf(`/api/bookPage/latest-likes-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`);
//     });
//
//     // 點擊 "Collects" 按鈕時，載入最新的 Collects 書籍
//     collectsButton.addEventListener('click', () => {
//         console.log("Loading latest collects by subCategory...");
//         loadBookshelf(`/api/bookPage/latest-collect-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`);
//     });
//
//     // 加載書籍的函數
//     function loadBookshelf(apiUrl) {
//         console.log(`Fetching data from ${apiUrl}...`);
//         fetch(apiUrl)
//             .then(response => {
//                 if (!response.ok) {
//                     throw new Error(`HTTP error! status: ${response.status}`);
//                 }
//                 return response.json();
//             })
//             .then(books => {
//                 containerCategory.innerHTML = ''; // 清空之前的內容
//
//                 if (books.length === 0) {
//                     containerCategory.innerHTML = '<p>No books found.</p>';
//                     return;
//                 }
//
//                 // 渲染書籍資訊
//                 books.forEach(book => {
//                     const bookDiv = document.createElement('div');
//                     bookDiv.classList.add('book-container');
//
//                     // 為每本書設置點擊事件，跳轉到 bookDetail 頁面
//                     bookDiv.addEventListener('click', function () {
//                         window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
//                     });
//
//                     bookDiv.innerHTML = `
//                         <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
//                         <div class="book-name">${book.bookName}</div>
//                     `;
//                     containerCategory.appendChild(bookDiv);
//                 });
//             })
//             .catch(error => console.error("Error fetching books:", error));
//     }
// });

/* bookshelfBySubCategory.js */
document.addEventListener("DOMContentLoaded", function () {
    // 提取 URL 中的 subCategory
    const urlParams = new URLSearchParams(window.location.search);
    const subCategory = urlParams.get('subCategory');

    const likesButton = document.getElementById('switch-likes');
    const collectsButton = document.getElementById('switch-collects');
    const containerCategory = document.getElementById('container-category');
    const paginationContainer = document.getElementById('pagination-bookshelf');
    const booksPerPage = 6; // 每頁顯示 6 本書
    let currentPage = 1;
    let totalBooks = [];

    if (!subCategory) {
        console.error("Missing subCategory in URL.");
        return;
    }

    // 預設加載 Likes 書籍
    loadBookshelf(`/api/bookPage/latest-likes-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`);

    // 點擊 "Likes" 按鈕時，載入最新的 Likes 書籍
    likesButton.addEventListener('click', () => {
        console.log("Loading latest likes by subCategory...");
        loadBookshelf(`/api/bookPage/latest-likes-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`);
    });

    // 點擊 "Collects" 按鈕時，載入最新的 Collects 書籍
    collectsButton.addEventListener('click', () => {
        console.log("Loading latest collects by subCategory...");
        loadBookshelf(`/api/bookPage/latest-collect-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`);
    });

    // 加載書籍的函數，並支援分頁
    function loadBookshelf(apiUrl) {
        console.log(`Fetching data from ${apiUrl}...`);
        fetch(apiUrl)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(books => {
                totalBooks = books;  // 保存書籍
                renderBooks(currentPage); // 初次加載時顯示當前頁面的書籍
                renderPagination(); // 渲染分頁按鈕
            })
            .catch(error => console.error("Error fetching books:", error));
    }

    // 渲染當前頁面的書籍
    function renderBooks(page) {
        containerCategory.innerHTML = ''; // 清空之前的內容

        const start = (page - 1) * booksPerPage;
        const end = page * booksPerPage;
        const booksToShow = totalBooks.slice(start, end); // 根據當前頁數提取書籍

        // 如果沒有書籍，顯示提示
        if (booksToShow.length === 0) {
            containerCategory.innerHTML = '<p>No books found.</p>';
            return;
        }

        // 渲染書籍資訊
        booksToShow.forEach(book => {
            const bookDiv = document.createElement('div');
            bookDiv.classList.add('book-container');

            // 為每本書設置點擊事件，跳轉到 bookDetail 頁面
            bookDiv.addEventListener('click', function () {
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
        paginationContainer.innerHTML = ''; // 清空現有的分頁按鈕

        const totalPages = Math.ceil(totalBooks.length / booksPerPage); // 計算總頁數

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage; // 禁用當前頁按鈕

            button.addEventListener('click', function () {
                currentPage = i; // 更新當前頁面
                renderBooks(currentPage); // 重新渲染當前頁面的書籍
                renderPagination(); // 重新渲染分頁按鈕
            });

            paginationContainer.appendChild(button);
        }
    }
});
