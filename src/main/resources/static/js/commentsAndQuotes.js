document.addEventListener("DOMContentLoaded", function() {
    const dynamicContainer = document.getElementById('dynamic-container');
    const switchCommentButton = document.getElementById('switch-comment');
    const switchQuoteButton = document.getElementById('switch-quote');
    let currentPage = 1;
    const itemsPerPage = 6;
    let totalItems = [];

    // 預設加載評論
    loadComments(currentPage);

    // 切換到評論
    switchCommentButton.addEventListener('click', () => {
        currentPage = 1;
        loadComments(currentPage);
    });

    // 切換到引言
    switchQuoteButton.addEventListener('click', () => {
        currentPage = 1;
        loadQuotes(currentPage);
    });

    // 加載評論
    async function loadComments(page) {
        const response = await fetch(`/api/bookPage/latest-comments`);
        totalItems = await response.json();
        renderItems(totalItems, page, renderComments);
    }

    // 加載引言
    async function loadQuotes(page) {
        const response = await fetch(`/api/bookPage/latest-quotes`);
        totalItems = await response.json();
        renderItems(totalItems, page, renderQuotes);
    }

    // 渲染項目 (評論或引言)
    function renderItems(items, page, renderFunction) {
        const totalPages = Math.ceil(items.length / itemsPerPage);
        const start = (page - 1) * itemsPerPage;
        const end = page * itemsPerPage;
        const itemsToShow = items.slice(start, end);

        dynamicContainer.innerHTML = ""; // 清空現有內容
        itemsToShow.forEach(item => renderFunction(item));
        renderPagination(totalPages, page);
    }

    // 渲染評論
    function renderComments(comment) {
        const commentDiv = document.createElement('div');
        commentDiv.classList.add('comment-container');

        const commentText = document.createElement('div');
        commentText.classList.add('comment-text');
        commentText.textContent = comment.comment;

        const userIdDiv = document.createElement('div');
        userIdDiv.classList.add('comment-user-id');
        userIdDiv.textContent = comment.userId;

        commentDiv.appendChild(commentText);
        commentDiv.appendChild(userIdDiv);
        dynamicContainer.appendChild(commentDiv);
    }

    // 渲染引言
    function renderQuotes(quote) {
        const quoteDiv = document.createElement('div');
        quoteDiv.classList.add('quote-container');

        const quoteText = document.createElement('div');
        quoteText.classList.add('quote-text');
        quoteText.textContent = quote.quote;

        const userIdDiv = document.createElement('div');
        userIdDiv.classList.add('quote-user-id');
        userIdDiv.textContent = quote.userId;

        quoteDiv.appendChild(quoteText);
        quoteDiv.appendChild(userIdDiv);
        dynamicContainer.appendChild(quoteDiv);
    }

    // 渲染分頁按鈕
    function renderPagination(totalPages, currentPage) {
        const paginationContainer = document.getElementById('pagination');
        paginationContainer.innerHTML = ""; // 清空現有分頁按鈕

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage;

            button.addEventListener('click', () => {
                currentPage = i;
                renderItems(totalItems, currentPage, currentPage === 1 ? renderComments : renderQuotes);
            });

            paginationContainer.appendChild(button);
        }
    }
});
