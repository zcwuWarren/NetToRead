/* bookshelfBySubCategory.js */
document.addEventListener("DOMContentLoaded", function() {
    // 提取 URL 中的 mainCategory 和 subCategory
    const urlParams = new URLSearchParams(window.location.search);
    const mainCategory = urlParams.get('mainCategory');
    const subCategory = urlParams.get('subCategory');

    if (mainCategory && subCategory) {
        // 調用後端 API 獲取最新按讚書籍
        fetch(`/api/bookPage/${encodeURIComponent(mainCategory)}/${encodeURIComponent(subCategory)}/latest-likes-by-category`)
            .then(response => response.json())
            .then(books => {
                const containerCategory = document.getElementById('container-category');
                containerCategory.innerHTML = ''; // 清空之前的內容

                // 渲染書籍資訊
                books.forEach(book => {
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
            })
            .catch(error => console.error("Error fetching latest liked books:", error));
    } else {
        console.error("Missing mainCategory or subCategory in URL.");
    }
});
