// bookDetailPage.js
// container a
document.addEventListener("DOMContentLoaded", async function() {
    const loadingContainer = document.querySelector('.loading-container');
    const containerA1 = document.getElementById('containerA1');

    // 顯示 loading
    function showLoading() {
        loadingContainer.style.display = 'flex';
    }

    // 隱藏 loading
    function hideLoading() {
        loadingContainer.style.display = 'none';
    }

    // 從 URL 中提取 bookId
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('bookId');
    const token = localStorage.getItem('jwtToken');
    let book;

    if (bookId && token) {
        checkUserInteraction(bookId);
    }

    if (bookId) {
        try {
            showLoading(); // 顯示 loading

            const response = await fetch(`/api/bookPage/getBookInfo?bookId=${bookId}`);
            book = await response.json();

            const containerA1 = document.getElementById('containerA1');
            const containerA2 = document.getElementById('containerA2');
            const containerA3 = document.getElementById('containerA3');
            const likeButton = document.getElementById('like-button');
            const collectButton = document.getElementById('collect-button');

            // 預加載圖片
            const img = new Image();
            img.onload = function() {
                // 圖片加載完成後，將其添加到 DOM 並淡入
                containerA1.innerHTML = ''; // 清空可能存在的佔位符
                containerA1.appendChild(img);
                setTimeout(() => {
                    img.classList.add('loaded');
                    hideLoading();
                }, 100); // 短暫延遲以確保過渡效果順滑
            };
            img.onerror = function() {
                console.error('無法加載圖片');
                hideLoading();
            };
            img.src = book.bookCover;
            img.alt = book.bookName;

            // 更新点赞和收藏按钮的裡面的数字
            document.getElementById('like-count').textContent = book.like;
            document.getElementById('collect-count').textContent = book.collect;

            // 更新點讚和收藏按鈕裡面的數字，並根據數值決定是否顯示
            updateCountDisplay('like-count', book.like);
            updateCountDisplay('collect-count', book.collect);

            // 渲染书籍信息
            document.getElementById('bookName').innerHTML = `<span>${book.bookName}</span>`;
            document.getElementById('author').innerHTML = `<span>${book.author}</span>`;
            document.getElementById('publisher').innerHTML = `<strong>出版社：</strong> <span>${book.publisher}</span>`;
            document.getElementById('publishDate').innerHTML = `<strong>出版日期：</strong> <span>${book.publishDate}</span>`;
            document.getElementById('isbn').innerHTML = `<strong>ISBN：</strong> <span>${book.isbn}</span>`;
            // document.getElementById('likes').innerHTML = `<strong>點讚數：</strong> <span>${book.like}</span>`;
            // document.getElementById('collects').innerHTML = `<strong>收藏數：</strong> <span>${book.collect}</span>`;
            document.getElementById('content').innerHTML = `<span>${book.content}</span>`;

            // 渲染書籍描述
            const bookDescription = `<div class="container-a-3-long-content">${book.description}</div>`;
            containerA3.innerHTML = bookDescription;

            // 圖書館主容器下拉選單查詢功能
            const librarySelect = document.getElementById('library-select');
            if (librarySelect) {
                librarySelect.addEventListener('change', function() {
                    const selectedLibrary = this.value;
                    if (selectedLibrary) {
                        if (book && book.isbn) {
                            const isbn = book.isbn;
                            const searchUrl = selectedLibrary.replace('${isbn}', isbn);
                            console.log('Opening library search URL:', searchUrl);
                            window.open(searchUrl, '_blank');
                        } else {
                            console.error('Book or ISBN not available:', book);
                        }
                    } else {
                        console.log('No library selected');
                    }
                });
            } else {
                console.error("Library select element not found");
            }

            // 按讚功能
            likeButton.addEventListener('click', async () => {
                const token = localStorage.getItem('jwtToken');
                if (!token) {
                    alert("Please log in to like the book.");
                    window.location.href = "account.html";
                    return;
                }

                try {
                    const response = await fetch(`/api/book/likeBook?bookId=${bookId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ token })
                    });
                    const result = await response.json();
                    if (response.ok) {
                        alert(result.message);
                        // 切換按鈕狀態
                        likeButton.classList.toggle('active');
                    } else {
                        alert(result.message);
                        // 切換按鈕狀態
                        likeButton.classList.remove('active');
                    }

                    // 無論是點讚還是取消點讚，都重新獲取書籍信息
                    const bookInfoResponse = await fetch(`/api/bookPage/getBookInfo?bookId=${bookId}`);
                    const updatedBook = await bookInfoResponse.json();

                    // 更新點讚數
                    const likeCountElement = document.getElementById('like-count');
                    likeCountElement.textContent = updatedBook.like;
                    updateCountDisplay('like-count', updatedBook.like);

                    // 更新全局的 book 對象
                    book = updatedBook;

                } catch (error) {
                    console.error("Error updating book like status:", error);
                }
            });

            // 收藏功能
            collectButton.addEventListener('click', async () => {
                const token = localStorage.getItem('jwtToken');
                if (!token) {
                    alert("Please log in to collect the book.");
                    window.location.href = "account.html";
                    return;
                }

                try {
                    const response = await fetch(`/api/book/collectBook?bookId=${bookId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ token })
                    });
                    const result = await response.json();
                    if (response.ok) {
                        alert(result.message);
                        // 切換按鈕狀態
                        collectButton.classList.toggle('active');
                    } else {
                        alert(result.message);
                        // 切換按鈕狀態
                        collectButton.classList.remove('active');
                    }

                    // 無論是收藏還是取消收藏，都重新獲取書籍信息
                    const bookInfoResponse = await fetch(`/api/bookPage/getBookInfo?bookId=${bookId}`);
                    const updatedBook = await bookInfoResponse.json();

                    // 更新收藏數
                    const collectCountElement = document.getElementById('collect-count');
                    collectCountElement.textContent = updatedBook.collect;
                    updateCountDisplay('collect-count', updatedBook.collect);

                    // 更新全局的 book 對象
                    book = updatedBook;

                } catch (error) {
                    console.error("Error updating book collect status:", error);
                }
            });

            hideLoading(); // 隱藏 loading
        } catch (error) {
            console.error("無法加載書籍詳細信息：", error);
            hideLoading(); // 發生錯誤時也要隱藏 loading
        }
    } else {
        console.error("缺少 bookId");
        hideLoading(); // 如果沒有 bookId，也隱藏 loading
    }

    // 新增的函數：更新計數顯示
    function updateCountDisplay(elementId, count) {
        const countElement = document.getElementById(elementId);
        if (count >= 1) {
            countElement.textContent = count;
            countElement.style.display = 'flex'; // 或 'block'，取決於您的樣式需求
        } else {
            countElement.style.display = 'none';
        }
    }

    // 確認 user 是否已經對書按讚或收藏
    async function checkUserInteraction(bookId) {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            console.log('User not logged in');
            return;
        }

        try {
            const response = await fetch(`/api/userPage/userInteraction?bookId=${bookId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ token: token })
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            updateButtonStates(data);
        } catch (error) {
            console.error('Error checking user interaction:', error);
        }
    }

    // 若 user 已按讚或收藏，更新按鈕變色狀態
    function updateButtonStates(interaction) {
        const likeButton = document.getElementById('like-button');
        const collectButton = document.getElementById('collect-button');

        if (interaction.liked) {
            likeButton.classList.add('active');
        } else {
            likeButton.classList.remove('active');
        }

        if (interaction.collected) {
            collectButton.classList.add('active');
        } else {
            collectButton.classList.remove('active');
        }
    }
});

    // combine contain b anc contain c
    document.addEventListener("DOMContentLoaded", async function() {
        let commentOffset = 0;
        let quoteOffset = 0;
        const limit = 6;
        let isLoading = false;
        let hasMoreComments = true;
        let hasMoreQuotes = true;
        let currentMode = 'comment'; // default  is Comment mode
        let isComposing = false; // prevent select word by pressing enter cause accidentally submit
        let isSubmitting = false; // for debounce


        const commentsContainer = document.getElementById('containerB');
        const bLeftButton = document.getElementById('b-left');
        const bRightButton = document.getElementById('b-right');
        const submitButton = document.getElementById('submit-button');
        const inputBox = document.getElementById('input-box');
        const bookshelfReviewTitleCommentQuote = document.getElementById('bookshelfReviewTitleCommentQuote').querySelector('h2');
        const loadingContainer = document.querySelector('.loading-container-b');

        let currentLoadFunction = loadComments;

        // 設置初始 active 狀態
        setActiveState(bLeftButton);
        updateBookshelfTitle('comments');

        // 顯示載入動畫
        function showLoading() {
            loadingContainer.style.display = 'flex';
            containerB.classList.remove('loaded');
        }

        // 隱藏載入動畫
        function hideLoading() {
            loadingContainer.style.display = 'none';
            containerB.classList.add('loaded');
        }

        const urlParams = new URLSearchParams(window.location.search);
        const bookId = urlParams.get('bookId');

        const token = localStorage.getItem('jwtToken');
        let userIdFromToken = null;

        // 解析 JWT Token 的函數
        function parseJwt(token) {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));

            return JSON.parse(jsonPayload);
        }

        // 檢查 JWT Token 是否過期
        function isTokenExpired(token) {
            if (!token) return true;
            const tokenParts = token.split('.');
            if (tokenParts.length !== 3) {
                return true;  // 無效的 token
            }

            const decodedPayload = JSON.parse(atob(tokenParts[1]));
            const expirationDate = new Date(decodedPayload.exp * 1000);  // exp 是以秒計算的
            const now = new Date();

            return expirationDate < now;  // 如果當前時間大於過期時間，token 已過期
        }

        // 检查并处理 token
        if (token) {
            if (isTokenExpired(token)) {
                console.error("JWT Token has expired.");
                localStorage.removeItem('jwtToken');
            } else {
                const decodedToken = parseJwt(token);
                userIdFromToken = decodedToken.userId;
            }
        }

        // 加载评论
        async function loadComments() {
            if (isLoading || !hasMoreComments) return;
            isLoading = true;
            showLoading();

            try {
                const response = await fetch(`/api/bookPage/switchToComment?bookId=${bookId}&offset=${commentOffset}&limit=${limit}`);
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
                console.error("无法加载评论：", error);
            } finally {
                isLoading = false;
                hideLoading();
            }
        }

        // 加载引用
        async function loadQuotes() {
            if (isLoading || !hasMoreQuotes) return;
            isLoading = true;
            showLoading();

            try {
                const response = await fetch(`/api/bookPage/switchToQuote?bookId=${bookId}&offset=${quoteOffset}&limit=${limit}`);
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
                console.error("无法加载引用：", error);
            } finally {
                isLoading = false;
                hideLoading();
            }
        }

        // toggleEdit, saveEdit, saveEditedComment, deleteComment, saveEditedQuote, deleteQuote 函数保持不变
        // 新增 toggleEdit
        function toggleEdit(textElement, editButton, id, type) {
            const isEditing = textElement.getAttribute('contenteditable') === 'true';
            if (isEditing) {
                saveEdit(textElement, editButton, id, type);
            } else {
                // 開始編輯
                textElement.setAttribute('contenteditable', 'true');
                textElement.focus();
                editButton.innerHTML = `
            <svg viewBox="0 0 24 24" width="24" height="24">
                <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
            </svg>
        `;

                // 添加編輯相關的事件監聽器
                textElement.addEventListener('compositionstart', () => {
                    isComposing = true;
                });

                textElement.addEventListener('compositionend', () => {
                    isComposing = false;
                });

                textElement.addEventListener('keydown', function(event) {
                    if (event.key === 'Enter' && !event.shiftKey && !isComposing) {
                        event.preventDefault();
                        saveEdit(textElement, editButton, id, type);
                    }
                });

                textElement.addEventListener('blur', function() {
                    if (!isComposing) {
                        saveEdit(textElement, editButton, id, type);
                    }
                });
            }
        }

        function saveEdit(textElement, editButton, id, type) {
            const newText = textElement.textContent.trim();
            if (newText !== '') {
                if (type === 'comment') {
                    saveEditedComment(id, newText, textElement);
                } else {
                    saveEditedQuote(id, newText, textElement);
                }
            }
            textElement.setAttribute('contenteditable', 'false');
            editButton.innerHTML = `
        <svg viewBox="0 0 24 24" width="24" height="24">
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
        </svg>
    `;

            // 移除所有添加的事件監聽器
            textElement.removeEventListener('compositionstart', () => {});
            textElement.removeEventListener('compositionend', () => {});
            textElement.removeEventListener('keydown', () => {});
            textElement.removeEventListener('blur', () => {});
        }

        // 編輯評論 支援 container 內編輯
        function saveEditedComment(commentId, newComment, commentTextElement) {
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
                    updatedComment: newComment
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.message) {
                        commentTextElement.textContent = newComment;
                    } else {
                        alert("Failed to edit comment.");
                    }
                })
                .catch(error => {
                    console.error("Error editing comment:", error);
                });
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

        // 編輯引言 支援 container 內編輯
        function saveEditedQuote(quoteId, newQuote, quoteTextElement) {
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
                    updatedQuote: newQuote
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.message) {
                        quoteTextElement.textContent = newQuote;
                    } else {
                        alert("Failed to edit quote.");
                    }
                })
                .catch(error => {
                    console.error("Error editing quote:", error);
                });
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

        // 渲染评论
        function renderComments(comments) {
            comments.forEach(comment => {
                const commentDiv = document.createElement('div');
                commentDiv.classList.add('comment-container');

                const contentDiv = document.createElement('div');
                contentDiv.classList.add('comment-content');

                const commentText = document.createElement('div');
                commentText.classList.add('comment-text');
                commentText.textContent = comment.comment;
                commentText.setAttribute('contenteditable', 'false');

                const userNameDiv = document.createElement('div');
                userNameDiv.classList.add('comment-user-name');
                userNameDiv.textContent = comment.userName;

                contentDiv.appendChild(commentText);
                contentDiv.appendChild(userNameDiv);

                commentDiv.appendChild(contentDiv);

                if (userIdFromToken && userIdFromToken === comment.userId) {
                    const buttonContainer = document.createElement('div');
                    buttonContainer.classList.add('button-container');

                    const editButton = document.createElement('button');
                    editButton.classList.add('icon-button');
                    editButton.innerHTML = `
                <svg viewBox="0 0 24 24" width="24" height="24">
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
            `;
                    editButton.addEventListener('click', (e) => {
                        e.stopPropagation();
                        toggleEdit(commentText, editButton, comment.id, 'comment');
                    });

                    const deleteButton = document.createElement('button');
                    deleteButton.classList.add('icon-button');
                    deleteButton.innerHTML = `
                <svg viewBox="0 0 24 24" width="24" height="24">
                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                </svg>
            `;
                    deleteButton.addEventListener('click', (e) => {
                        e.stopPropagation();
                        deleteComment(comment.id, commentDiv);
                    });

                    buttonContainer.appendChild(editButton);
                    buttonContainer.appendChild(deleteButton);
                    commentDiv.appendChild(buttonContainer);
                }

                commentsContainer.appendChild(commentDiv);
            });
        }

        // 渲染引用
        function renderQuotes(quotes) {
            quotes.forEach(quote => {
                const quoteDiv = document.createElement('div');
                quoteDiv.classList.add('quote-container');

                const contentDiv = document.createElement('div');
                contentDiv.classList.add('quote-content');

                const quoteText = document.createElement('div');
                quoteText.classList.add('quote-text');
                quoteText.textContent = quote.quote;
                quoteText.setAttribute('contenteditable', 'false');

                const userNameDiv = document.createElement('div');
                userNameDiv.classList.add('quote-user-name');
                userNameDiv.textContent = quote.userName;

                contentDiv.appendChild(quoteText);
                contentDiv.appendChild(userNameDiv);

                quoteDiv.appendChild(contentDiv);

                if (userIdFromToken && userIdFromToken === quote.userId) {
                    const buttonContainer = document.createElement('div');
                    buttonContainer.classList.add('button-container');

                    const editButton = document.createElement('button');
                    editButton.classList.add('icon-button');
                    editButton.innerHTML = `
                <svg viewBox="0 0 24 24" width="24" height="24">
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
            `;
                    editButton.addEventListener('click', (e) => {
                        e.stopPropagation();
                        toggleEdit(quoteText, editButton, quote.id, 'quote');
                    });

                    const deleteButton = document.createElement('button');
                    deleteButton.classList.add('icon-button');
                    deleteButton.innerHTML = `
                <svg viewBox="0 0 24 24" width="24" height="24">
                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                </svg>
            `;
                    deleteButton.addEventListener('click', (e) => {
                        e.stopPropagation();
                        deleteQuote(quote.id, quoteDiv);
                    });

                    buttonContainer.appendChild(editButton);
                    buttonContainer.appendChild(deleteButton);
                    quoteDiv.appendChild(buttonContainer);
                }

                commentsContainer.appendChild(quoteDiv);
            });
        }

        function handleScroll() {
            const scrollPosition = window.innerHeight + window.pageYOffset;
            const containerBottom = commentsContainer.offsetTop + commentsContainer.offsetHeight;

            if (scrollPosition >= containerBottom - 200 && !isLoading) {
                currentLoadFunction();
            }
        }

        window.addEventListener('scroll', handleScroll);

        function updateUIForMode(mode) {
            if (mode === 'comment') {
                inputBox.placeholder = "Please login to write a Comment";
            } else {
                inputBox.placeholder = "Please login to write a Quote";
            }
        }

        // 初始化UI
        updateUIForMode(currentMode);

        // 切换到评论
        bLeftButton.addEventListener('click', () => {
            currentMode = 'comment';
            updateUIForMode(currentMode);
            commentsContainer.innerHTML = '';
            commentOffset = 0;
            hasMoreComments = true;
            currentLoadFunction = loadComments;
            setActiveState(bLeftButton);
            removeActiveState(bRightButton);
            updateBookshelfTitle('comments');
            loadComments();
        });

        // 切换到引用
        bRightButton.addEventListener('click', () => {
            currentMode = 'quote';
            updateUIForMode(currentMode);
            commentsContainer.innerHTML = '';
            quoteOffset = 0;
            hasMoreQuotes = true;
            currentLoadFunction = loadQuotes;
            setActiveState(bRightButton);
            removeActiveState(bLeftButton);
            updateBookshelfTitle('quotes')
            loadQuotes();
        });

        // debounce function
        function debounce(func, wait) {
            let timeout;
            return function executedFunction(...args) {
                const later = () => {
                    clearTimeout(timeout);
                    func(...args);
                };
                clearTimeout(timeout);
                timeout = setTimeout(later, wait);
            };
        }

        // submit with debounce
        const debouncedHandleSubmit = debounce(async () => {
            if (isSubmitting) return;
            isSubmitting = true;

            const commentOrQuote = inputBox.value.trim();

            if (!token) {
                alert("Please log in to submit.");
                window.location.href = "account.html";
                isSubmitting = false;
                return;
            }

            if (commentOrQuote === "") {
                alert("Input cannot be empty.");
                isSubmitting = false;
                return;
            }

            try {
                let apiUrl;
                let requestBody;

                if (currentMode === 'comment') {
                    apiUrl = `/api/book/addComment`;
                    requestBody = { comment: commentOrQuote, token: token };
                } else {
                    apiUrl = `/api/book/addQuote`;
                    requestBody = { quote: commentOrQuote, token: token };
                }

                const response = await fetch(`${apiUrl}?bookId=${bookId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestBody)
                });

                const result = await response.json();

                if (response.ok) {
                    inputBox.value = ""; // 清空输入框

                    // 重新加载评论或引用
                    commentsContainer.innerHTML = ''; // 清空容器
                    if (currentMode === 'comment') {
                        commentOffset = 0;
                        hasMoreComments = true;
                        await loadComments(1);
                    } else {
                        quoteOffset = 0;
                        hasMoreQuotes = true;
                        await loadQuotes(1);
                    }
                    // 滾動到容器頂部以顯示新添加的內容
                    commentsContainer.scrollTop = 0;
                } else {
                    alert(result.message || "Failed to submit.");
                }
            } catch (error) {
                console.error("Error submitting data:", error);
                alert("Failed to submit. Please try again.");
            } finally {
                isSubmitting = false;
            }
        }, 300); // 300毫秒的延遲

        inputBox.addEventListener('compositionstart', () => {
            isComposing = true;
        });

        inputBox.addEventListener('compositionend', () => {
            isComposing = false;
        });

        inputBox.addEventListener('keydown', (event) => {
            if (event.key === 'Enter' && !event.shiftKey && !isComposing) {
                event.preventDefault();
                debouncedHandleSubmit();
            }
        });

        submitButton.addEventListener('click', (event) => {
            event.preventDefault();
            debouncedHandleSubmit();
        });

        // 初始加载评论
        loadComments();

        // 設置 active 狀態
        function setActiveState(button) {
            button.classList.add('active');
            button.style.backgroundColor = '#B6ADA5';
            button.style.color = '#041723';
        }

        // 移除 active 狀態
        function removeActiveState(button) {
            button.classList.remove('active');
            button.style.backgroundColor = '';
            button.style.color = '';
        }

        // 更新書架標題
        function updateBookshelfTitle(type) {
            if (type === 'comments') {
                bookshelfReviewTitleCommentQuote.textContent = '最新評論';
            } else if (type === 'quotes') {
                bookshelfReviewTitleCommentQuote.textContent = '最新引言';
            }
        }
});