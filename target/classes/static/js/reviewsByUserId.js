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

    let userIdFromToken = null;

    // 如果有 JWT，解析用戶 ID
    if (token) {
        const decodedToken = parseJwt(token);
        userIdFromToken = decodedToken.userId; // 解析出 userId
    }

    // 解析 JWT Token 的函數
    function parseJwt(token) {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
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

    // 編輯評論
    function editComment(commentId, commentTextElement) {
        const newComment = prompt("Edit your comment:", commentTextElement.textContent);
        if (newComment) {
            const token = localStorage.getItem('jwtToken');
            fetch(`/api/book/editComment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    id: commentId,
                    token: token,
                    updatedComment: newComment })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.message) {
                        commentTextElement.textContent = newComment; // 更新頁面上的評論
                    } else {
                        alert("Failed to edit comment.");
                    }
                })
                .catch(error => {
                    console.error("Error editing comment:", error);
                });
        }
    }

    // 刪除評論
    function deleteComment(commentId, commentDiv) {
        const token = localStorage.getItem('jwtToken');
        fetch('/api/book/deleteComment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                id: commentId,
                token: token
            })
        })
            .then(response => response.json())
            .then(data => {
                // todo 注意回傳 key 是message
                if (data.message) {
                    commentDiv.remove();
                } else {
                    alert("Failed to delete comment.");
                }
            })
            .catch(error => {
                console.error("Error deleting comment:", error);
            });
    }

    // 編輯引言
    function editQuote(quoteId, quoteTextElement) {
        const newQuote = prompt("Edit your comment:", quoteTextElement.textContent);
        if (newQuote) {
            const token = localStorage.getItem('jwtToken');
            fetch(`/api/book/editQuote`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    id: quoteId,
                    token: token,
                    updatedQuote: newQuote })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.message) {
                        quoteTextElement.textContent = newQuote; // 更新頁面上的評論
                    } else {
                        alert("Failed to edit quote.");
                    }
                })
                .catch(error => {
                    console.error("Error editing quote:", error);
                });
        }
    }

    // 刪除引言
    function deleteQuote(quoteId, quoteDiv) {
        const token = localStorage.getItem('jwtToken');
        fetch('/api/book/deleteQuote', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                id: quoteId,
                token: token
            })
        })
            .then(response => response.json())
            .then(data => {
                // 注意回傳 key 是message
                if (data.message) {
                    quoteDiv.remove(); // 從頁面中刪除該引言
                } else {
                    alert("Failed to delete quote.");
                }
            })
            .catch(error => {
                console.error("Error deleting quote:", error);
            });
    }

    // 渲染評論
    function renderComments(comments) {
        commentsContainer.innerHTML = "";  // 清空現有評論

        comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            commentDiv.classList.add('comment-container');

            // 包裹 commentText 和 bookName 的容器
            const commentContentDiv = document.createElement('div');
            commentContentDiv.classList.add('comment-content-container');
            commentContentDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${comment.bookId}`;
            });

            const commentText = document.createElement('div');
            commentText.classList.add('comment-text');
            commentText.textContent = comment.comment;  // 顯示評論文字

            const bookName = document.createElement('div');
            bookName.classList.add('comment-book-name');
            bookName.textContent = comment.bookName;  // 顯示書名

            commentContentDiv.appendChild(commentText);
            commentContentDiv.appendChild(bookName);

            // 包裹 editButton 和 deleteButton 的容器
            const buttonContainer = document.createElement('div');
            buttonContainer.classList.add('button-container');

            // 如果 JWT 中的 userId 與評論的 userId 匹配，顯示編輯和刪除按鈕
            if (userIdFromToken && userIdFromToken === comment.userId) {
                const editButton = document.createElement('button');
                editButton.classList.add('delete-edit-button');
                editButton.textContent = "Edit";
                editButton.addEventListener('click', (event) => {
                    event.stopPropagation();  // 阻止事件冒泡到 commentContentDiv
                    editComment(comment.id, commentText);
                });

                const deleteButton = document.createElement('button');
                deleteButton.classList.add('delete-edit-button'); // 為 button 添加 class
                deleteButton.textContent = "Delete";
                deleteButton.addEventListener('click', (event) => {
                    event.stopPropagation();  // 阻止事件冒泡到 commentContentDiv
                    deleteComment(comment.id, commentDiv);
                });

                buttonContainer.appendChild(editButton);
                buttonContainer.appendChild(deleteButton);
            }

            // 將 commentContentDiv 和 buttonContainer 添加到主 commentDiv 中
            commentDiv.appendChild(commentContentDiv);
            commentDiv.appendChild(buttonContainer);
            commentsContainer.appendChild(commentDiv);
        });
    }

    // 渲染引用
    function renderQuotes(quotes) {
        commentsContainer.innerHTML = "";  // 清空現有引用

        quotes.forEach(quote => {
            const quoteDiv = document.createElement('div');
            quoteDiv.classList.add('quote-container');

            // 包裹 quoteText 和 bookName 的容器
            const quoteContentDiv = document.createElement('div');
            quoteContentDiv.classList.add('quote-content-container');
            quoteContentDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${quote.bookId}`;
            });

            const quoteText = document.createElement('div');
            quoteText.classList.add('quote-text');
            quoteText.textContent = quote.quote;  // 顯示引用文字

            const bookName = document.createElement('div');
            bookName.classList.add('comment-book-name');
            bookName.textContent = quote.bookName;  // 顯示書名

            quoteContentDiv.appendChild(quoteText);
            quoteContentDiv.appendChild(bookName);

            // 包裹 editButton 和 deleteButton 的容器
            const buttonContainer = document.createElement('div');
            buttonContainer.classList.add('button-container');

            // 如果 JWT 中的 userId 與引言的 userId 匹配，顯示刪除按鈕
            if (userIdFromToken && userIdFromToken === quote.userId) {
                const editButton = document.createElement('button');
                editButton.classList.add('delete-edit-button');
                editButton.textContent = "Edit";
                editButton.addEventListener('click', () => {
                    editQuote(quote.id, quoteText);
                });

                const deleteButton = document.createElement('button');
                deleteButton.classList.add('delete-edit-button');
                deleteButton.textContent = "Delete";
                deleteButton.addEventListener('click', () => {
                    deleteQuote(quote.id, quoteDiv);
                });

                buttonContainer.appendChild(editButton);
                buttonContainer.appendChild(deleteButton);
            }
            // 將 quoteContentDiv 和 buttonContainer 添加到主 quoteDiv 中
            quoteDiv.appendChild(quoteContentDiv);
            quoteDiv.appendChild(buttonContainer);
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
