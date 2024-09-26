/* copy from bookIdDetail */
// container b
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

            // 點擊 comment 跳轉至該 bookId 書籍頁面
            commentDiv.addEventListener('click', function (){
                window.location.href = `/bookDetail.html?bookId=${comment.bookId}`;
            });

            const commentText = document.createElement('div');
            commentText.classList.add('comment-text');
            commentText.textContent = comment.comment;  // 顯示評論文字

            const bookName = document.createElement('div');
            bookName.classList.add('comment-book-name');
            bookName.textContent = comment.bookName;  // 顯示書名

            commentDiv.appendChild(commentText);
            commentDiv.appendChild(bookName);
            commentsContainer.appendChild(commentDiv);
        });
    }

    // 渲染引用
    function renderQuotes(quotes) {
        commentsContainer.innerHTML = "";  // 清空現有引用

        quotes.forEach(quote => {
            const quoteDiv = document.createElement('div');
            quoteDiv.classList.add('quote-container');

            // 點擊 quote 跳轉至該 bookId 書籍頁面
            quoteDiv.addEventListener('click', function (){
                window.location.href = `/bookDetail.html?bookId=${quote.bookId}`;
            });

            const quoteText = document.createElement('div');
            quoteText.classList.add('quote-text');
            quoteText.textContent = quote.quote;  // 顯示引用文字

            const bookName = document.createElement('div');
            bookName.classList.add('comment-book-name');
            bookName.textContent = quote.bookName;  // 顯示書名

            quoteDiv.appendChild(quoteText);
            quoteDiv.appendChild(bookName);
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
