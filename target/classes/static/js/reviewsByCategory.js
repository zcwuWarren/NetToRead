// reviewsByCategory.js
document.addEventListener("DOMContentLoaded", async function() {
    let commentOffset = 0;
    let quoteOffset = 0;
    const limit = 50;
    let isLoading = false;
    let hasMoreComments = true;
    let hasMoreQuotes = true;

    const containerB = document.getElementById('containerB');
    const bLeftButton = document.getElementById('b-left');
    const bRightButton = document.getElementById('b-right');
    let currentLoadFunction = loadComments;

    const urlParams = new URLSearchParams(window.location.search);
    const subCategory = urlParams.get('subCategory');

    // 加載評論
    async function loadComments() {
        if (isLoading || !hasMoreComments) return;
        isLoading = true;

        try {
            const response = await fetch(`/api/bookPage/latest-comments-by-subCategory?subCategory=${subCategory}&offset=${commentOffset}&limit=${limit}`);
            const comments = await response.json();

            if (comments.length > 0) {
                renderComments(comments);
                commentOffset += comments.length;
                if (comments.length < limit) {
                    hasMoreComments = false;
                }
            } else {
                hasMoreComments = false;
            }
        } catch (error) {
            console.error("無法載入評論：", error);
        } finally {
            isLoading = false;
        }
    }

    // 加載引用
    async function loadQuotes() {
        if (isLoading || !hasMoreQuotes) return;
        isLoading = true;

        try {
            const response = await fetch(`/api/bookPage/latest-quotes-by-subCategory?subCategory=${subCategory}&offset=${quoteOffset}&limit=${limit}`);
            const quotes = await response.json();

            if (quotes.length > 0) {
                renderQuotes(quotes);
                quoteOffset += quotes.length;
                if (quotes.length < limit) {
                    hasMoreQuotes = false;
                }
            } else {
                hasMoreQuotes = false;
            }
        } catch (error) {
            console.error("無法載入引用：", error);
        } finally {
            isLoading = false;
        }
    }

    // 渲染評論
    function renderComments(comments) {
        comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            commentDiv.classList.add('comment-container');

            commentDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${comment.bookId}`;
            });

            const commentText = document.createElement('div');
            commentText.classList.add('comment-text');
            commentText.textContent = comment.comment;

            const bookName = document.createElement('div');
            bookName.classList.add('comment-book-name');
            bookName.textContent = comment.bookName;

            commentDiv.appendChild(commentText);
            commentDiv.appendChild(bookName);
            containerB.appendChild(commentDiv);
        });
    }

    // 渲染引用
    function renderQuotes(quotes) {
        quotes.forEach(quote => {
            const quoteDiv = document.createElement('div');
            quoteDiv.classList.add('quote-container');

            quoteDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${quote.bookId}`;
            });

            const quoteText = document.createElement('div');
            quoteText.classList.add('quote-text');
            quoteText.textContent = quote.quote;

            const bookName = document.createElement('div');
            bookName.classList.add('quote-book-name');
            bookName.textContent = quote.bookName;

            quoteDiv.appendChild(quoteText);
            quoteDiv.appendChild(bookName);
            containerB.appendChild(quoteDiv);
        });
    }

    function handleScroll() {
        const scrollPosition = window.innerHeight + window.pageYOffset;
        const containerBottom = containerB.offsetTop + containerB.offsetHeight;

        if (scrollPosition >= containerBottom - 200 && !isLoading) {
            currentLoadFunction();
        }
    }

    window.addEventListener('scroll', handleScroll);

    // 初始加載評論
    loadComments();

    // 切換到評論
    bLeftButton.addEventListener('click', () => {
        containerB.innerHTML = '';
        commentOffset = 0;
        hasMoreComments = true;
        currentLoadFunction = loadComments;
        loadComments();
    });

    // 切換到引用
    bRightButton.addEventListener('click', () => {
        containerB.innerHTML = '';
        quoteOffset = 0;
        hasMoreQuotes = true;
        currentLoadFunction = loadQuotes;
        loadQuotes();
    });
});