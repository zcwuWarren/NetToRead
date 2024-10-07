document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('keyword');
    const page = parseInt(urlParams.get('page') || '1');
    const size = 40; // 每页显示的结果数

    if (keyword) {
        document.querySelector('#search-keyword span').textContent = keyword;
        fetchSearchResults(keyword, page, size);
    }
});

async function fetchSearchResults(keyword, page, size) {
    try {
        const response = await fetch(`/api/bookPage/searchBooks?keyword=${encodeURIComponent(keyword)}&page=${page - 1}&size=${size}`);
        const data = await response.json();
        renderSearchResults(data.content);
        renderPagination(data.totalPages, page);
    } catch (error) {
        console.error('Error fetching search results:', error);
    }
}

// function renderSearchResults(books) {
//     const container = document.getElementById('books-container');
//     container.innerHTML = '';
//
//     books.forEach(book => {
//         const bookElement = document.createElement('div');
//         bookElement.className = 'book-item';
//         bookElement.innerHTML = `
//             <div class="book-cover">
//                 <img src="${book.bookCover}" alt="${book.bookName}">
//             </div>
//             <div class="book-name">${book.bookName}</div>
//             <div class="book-author">${book.author}</div>
//         `;
//         bookElement.addEventListener('click', () => {
//             window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
//         });
//         container.appendChild(bookElement);
//     });
// }

function renderPagination(totalPages, currentPage) {
    const paginationContainer = document.getElementById('pagination');
    paginationContainer.innerHTML = '';

    const urlParams = new URLSearchParams(window.location.search);

    for (let i = 1; i <= totalPages; i++) {
        const button = document.createElement('button');
        button.textContent = i;
        button.disabled = i === currentPage;
        button.addEventListener('click', () => {
            urlParams.set('page', i);
            window.location.search = urlParams.toString();
        });
        paginationContainer.appendChild(button);
    }
}

// 在原有的 index.html 中添加以下代码来处理搜索
document.getElementById('search-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        performSearch();
    }
});

document.querySelector('.search-icon').addEventListener('click', performSearch);

function performSearch() {
    const keyword = document.getElementById('search-input').value.trim();
    if (keyword) {
        window.location.href = `/searchResult.html?keyword=${encodeURIComponent(keyword)}`;
    }
}

function renderSearchResults(books) {
    const container = document.getElementById('books-container');
    container.innerHTML = '';

    books.forEach(book => {
        const bookElement = document.createElement('div');
        bookElement.className = 'book-item';
        bookElement.innerHTML = `
            <div class="book-cover">
                <img src="${book.bookCover}" alt="${book.bookName}">
            </div>
            <div class="book-info">
                <div class="book-name-container">
                    <div class="book-name">${book.bookName}</div>
                </div>
                <div class="book-author-container">
                    <div class="book-author">${book.author}</div>
                </div>
            </div>
        `;
        bookElement.addEventListener('click', () => {
            window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
        });
        container.appendChild(bookElement);
    });
}