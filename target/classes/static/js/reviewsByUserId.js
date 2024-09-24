/* copy from bookIdDetail */
// container b
// document.addEventListener("DOMContentLoaded", async function() {
//     let currentPage = 1;
//     const commentsPerPage = 6;
//     let totalComments = [];
//     let totalQuotes = [];
//
//     const commentsContainer = document.getElementById('containerB');
//     const paginationContainer = document.getElementById('pagination');
//     const bLeftButton = document.getElementById('b-left');
//     const bRightButton = document.getElementById('b-right');
//     let currentLoadFunction = loadComments; // 追踪當前的加載函數，默認為評論加載
//
//     // 提取 localStorage 中的 jwtToken
//     const token = localStorage.getItem('jwtToken');
//     if (!token) {
//         console.error("No JWT token found in localStorage");
//         return;
//     }
//
//     // 加載評論
//     async function loadComments(page) {
//         try {
//             const urlParams = new URLSearchParams(window.location.search);
//             const subCategory = urlParams.get('subCategory');
//             const response = await fetch(`/api/userPage/myComment`);
//             totalComments = await response.json();
//             console.log(totalComments);  // 檢查 API 返回的數據
//             // console.log("Book ID: ", bookId);
//             console.log("Loaded Comments:", totalComments);  // 確認返回的數據
//
//
//             // 計算總頁數
//             const totalPages = Math.ceil(totalComments.length / commentsPerPage);
//
//             // 分頁顯示
//             const start = (page - 1) * commentsPerPage;
//             const end = page * commentsPerPage;
//             const commentsToShow = totalComments.slice(start, end);
//
//             renderComments(commentsToShow);
//             renderPagination(totalPages, page, loadComments);  // 修改這行，傳入正確的加載函數
//         } catch (error) {
//             console.error("無法載入評論：", error);
//         }
//     }
//
//     // 加載引用
//     async function loadQuotes(page) {
//         try {
//             const urlParams = new URLSearchParams(window.location.search);
//             const subCategory = urlParams.get('subCategory');
//             const response = await fetch(`/api/userPage/myQuote`);
//             totalQuotes = await response.json();
//             console.log(totalQuotes); // 檢查 API 返回的數據
//
//             const totalPages = Math.ceil(totalQuotes.length / commentsPerPage);
//             const start = (page - 1) * commentsPerPage;
//             const end = page * commentsPerPage;
//             const quotesToShow = totalQuotes.slice(start, end);
//
//             renderQuotes(quotesToShow);
//             renderPagination(totalPages, page, loadQuotes);
//         } catch (error) {
//             console.error("無法載入引用：", error);
//         }
//     }
//
//     // 渲染評論
//     function renderComments(comments) {
//         const commentsContainer = document.getElementById('containerB');
//         commentsContainer.innerHTML = "";  // 清空現有評論
//
//         comments.forEach(comment => {
//             console.log("Rendering comment:", comment);  // 確認每個評論是否被渲染
//             const commentDiv = document.createElement('div');
//             commentDiv.classList.add('comment-container');
//
//             const commentText = document.createElement('div');
//             commentText.classList.add('comment-text');
//             commentText.textContent = comment.comment;  // 顯示評論文字
//
//             const userIdDiv = document.createElement('div');
//             userIdDiv.classList.add('comment-user-id');
//             userIdDiv.textContent = comment.userId;  // 顯示用戶 ID
//
//             commentDiv.appendChild(commentText);
//             commentDiv.appendChild(userIdDiv);
//             commentsContainer.appendChild(commentDiv);
//         });
//     }
//
//     // 渲染引用
//     function renderQuotes(quotes) {
//         commentsContainer.innerHTML = "";  // 清空現有引用
//
//         quotes.forEach(quote => {
//             const quoteDiv = document.createElement('div');
//             quoteDiv.classList.add('quote-container');
//
//             const quoteText = document.createElement('div');
//             quoteText.classList.add('quote-text');
//             quoteText.textContent = quote.quote;  // 顯示引用文字
//
//             const userIdDiv = document.createElement('div');
//             userIdDiv.classList.add('quote-user-id');
//             userIdDiv.textContent = quote.userId;  // 顯示用戶 ID
//
//             quoteDiv.appendChild(quoteText);
//             quoteDiv.appendChild(userIdDiv);
//             commentsContainer.appendChild(quoteDiv);
//         });
//     }
//
//     // 渲染分頁按鈕
//     function renderPagination(totalPages, currentPage) {
//         const paginationContainer = document.getElementById('pagination');
//         paginationContainer.innerHTML = "";
//
//         for (let i = 1; i <= totalPages; i++) {
//             const button = document.createElement('button');
//             button.textContent = i;
//             button.disabled = i === currentPage;
//
//             button.addEventListener('click', () => {
//                 currentPage = i;
//                 loadComments(currentPage);  // 重新加載對應頁的評論
//             });
//
//             paginationContainer.appendChild(button);
//         }
//     }
//
//
//     // 預設加載評論
//     loadComments(currentPage);
//
//     // 切換到評論
//     bLeftButton.addEventListener('click', () => {
//         currentPage = 1;
//         loadComments(currentPage);
//     });
//
//     // 切換到引用
//     bRightButton.addEventListener('click', () => {
//         currentPage = 1;
//         loadQuotes(currentPage);
//     });
// });

document.addEventListener("DOMContentLoaded", async function() {
    let currentPage = 1;
    const itemsPerPage = 6;
    let totalComments = [];
    let totalQuotes = [];

    const commentsContainer = document.getElementById('containerB');
    const paginationContainer = document.getElementById('pagination');
    const bLeftButton = document.getElementById('b-left');
    const bRightButton = document.getElementById('b-right');

    // 提取 localStorage 中的 jwtToken
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        console.error("No JWT token found in localStorage");
        return;
    }

    // 加載評論
    async function loadComments(page) {
        try {
            const response = await fetch(`/api/userPage/myComment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ token: token })
            });
            totalComments = await response.json();
            console.log("Loaded Comments:", totalComments);  // 檢查 API 返回的數據

            // 計算總頁數
            const totalPages = Math.ceil(totalComments.length / itemsPerPage);

            // 分頁顯示
            const start = (page - 1) * itemsPerPage;
            const end = page * itemsPerPage;
            const commentsToShow = totalComments.slice(start, end);

            renderComments(commentsToShow);
            renderPagination(totalPages, page, loadComments);  // 傳入正確的加載函數
        } catch (error) {
            console.error("無法載入評論：", error);
        }
    }

    // 加載引用
    async function loadQuotes(page) {
        try {
            const response = await fetch(`/api/userPage/myQuote`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ token: token })
            });
            totalQuotes = await response.json();
            console.log("Loaded Quotes:", totalQuotes); // 檢查 API 返回的數據

            const totalPages = Math.ceil(totalQuotes.length / itemsPerPage);
            const start = (page - 1) * itemsPerPage;
            const end = page * itemsPerPage;
            const quotesToShow = totalQuotes.slice(start, end);

            renderQuotes(quotesToShow);
            renderPagination(totalPages, page, loadQuotes);
        } catch (error) {
            console.error("無法載入引用：", error);
        }
    }

    // 渲染評論
    function renderComments(comments) {
        commentsContainer.innerHTML = "";  // 清空現有評論

        comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            commentDiv.classList.add('comment-container');

            const commentText = document.createElement('div');
            commentText.classList.add('comment-text');
            commentText.textContent = comment.comment;  // 顯示評論文字

            const userIdDiv = document.createElement('div');
            userIdDiv.classList.add('comment-user-id');
            userIdDiv.textContent = comment.userId;  // 顯示用戶 ID

            commentDiv.appendChild(commentText);
            commentDiv.appendChild(userIdDiv);
            commentsContainer.appendChild(commentDiv);
        });
    }

    // 渲染引用
    function renderQuotes(quotes) {
        commentsContainer.innerHTML = "";  // 清空現有引用

        quotes.forEach(quote => {
            const quoteDiv = document.createElement('div');
            quoteDiv.classList.add('quote-container');

            const quoteText = document.createElement('div');
            quoteText.classList.add('quote-text');
            quoteText.textContent = quote.quote;  // 顯示引用文字

            const userIdDiv = document.createElement('div');
            userIdDiv.classList.add('quote-user-id');
            userIdDiv.textContent = quote.userId;  // 顯示用戶 ID

            quoteDiv.appendChild(quoteText);
            quoteDiv.appendChild(userIdDiv);
            commentsContainer.appendChild(quoteDiv);
        });
    }

    // 渲染分頁按鈕
    function renderPagination(totalPages, currentPage, loadFunction) {
        paginationContainer.innerHTML = "";

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage;

            button.addEventListener('click', () => {
                currentPage = i;
                loadFunction(currentPage);  // 根據按鈕的頁面數重新加載對應頁的評論或引用
            });

            paginationContainer.appendChild(button);
        }
    }

    // 預設加載評論
    loadComments(currentPage);

    // 切換到評論
    bLeftButton.addEventListener('click', () => {
        currentPage = 1;
        loadComments(currentPage);
    });

    // 切換到引用
    bRightButton.addEventListener('click', () => {
        currentPage = 1;
        loadQuotes(currentPage);
    });
});
