// categoryNavBar.js
document.addEventListener("DOMContentLoaded", function() {
    // 從 API 獲取分類資料
    fetch("/api/bookPage/categories")
        .then(response => response.json())
        .then(categories => {
            const mainCategoryContainer = document.getElementById('mainCategoryContainer');
            const mainCategories = {};

            // 整理主分類與次分類的資料
            categories.forEach(item => {
                const { mainCategory, subCategory } = item;
                if (!mainCategories[mainCategory]) {
                    mainCategories[mainCategory] = [];
                }
                mainCategories[mainCategory].push(subCategory);
            });

            // 動態生成主分類和次分類
            Object.keys(mainCategories).forEach(mainCategory => {
                const mainCategoryDiv = document.createElement('div');
                mainCategoryDiv.classList.add('category-item');
                mainCategoryDiv.innerText = mainCategory;

                // 生成次分類容器
                const subCategoryContainer = document.createElement('div');
                subCategoryContainer.classList.add('sub-category-container');

                mainCategories[mainCategory].forEach(subCategory => {
                    const subCategoryDiv = document.createElement('div');
                    subCategoryDiv.classList.add('sub-category-item');
                    subCategoryDiv.innerText = subCategory;

                    // 點擊 sub_category 時跳轉到 category.html，並附帶參數
                    subCategoryDiv.addEventListener('click', function() {
                        const url = `category.html?mainCategory=${encodeURIComponent(mainCategory)}&subCategory=${encodeURIComponent(subCategory)}`;
                        window.location.href = url;
                    });

                    subCategoryContainer.appendChild(subCategoryDiv);
                });

                mainCategoryDiv.appendChild(subCategoryContainer);
                mainCategoryContainer.appendChild(mainCategoryDiv);
            });
        })
        .catch(error => console.error("Error fetching categories:", error));
});

document.addEventListener('DOMContentLoaded', function() {
    const suggestionBox = document.getElementById('suggestion-box');
    suggestionBox.style.display = 'none';  // 初始化时隐藏建议框
    console.log('Suggestion box initially hidden');
});

document.getElementById('search-input').addEventListener('input', function() {
    const keyword = this.value.trim();
    const suggestionBox = document.getElementById('suggestion-box');

    if (keyword.length >= 1) {
        fetchSuggestions(keyword);

    } else {
        suggestionBox.innerHTML = ''; // 如果 keyword 為空，清空建議框
        suggestionBox.style.display = 'none'; // 沒有關鍵字時隱藏建議框
    }
});

// 添加 focus 事件來重新觸發搜索
document.getElementById('search-input').addEventListener('focus', function() {
    const keyword = this.value.trim();
    const suggestionBox = document.getElementById('suggestion-box');

    if (keyword.length >= 1) {
        fetchSuggestions(keyword); // 如果已有內容，重新執行搜索
    }
});

function fetchSuggestions(keyword) {
    fetch(`/api/bookPage/getAutocomplete?keyword=${encodeURIComponent(keyword)}`)
        .then(response => response.json())
        .then(books => {
            displayAutocompleteSuggestions(books);
        });
}

function displayAutocompleteSuggestions(books) {
    const suggestionBox = document.getElementById('suggestion-box');
    suggestionBox.innerHTML = '';
    suggestionBox.style.display = 'block';  // 确保建议框可见

    books.forEach(book => {
        const suggestionItem = document.createElement('div');
        suggestionItem.textContent = book.bookName;
        suggestionItem.classList.add('suggestion-item');
        suggestionBox.appendChild(suggestionItem);

        // 點擊建議項目後跳轉到書籍詳細頁面
        suggestionItem.addEventListener('click', () => {
            window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
        });
    });
}

// 監聽鍵盤退格和刪除鍵
document.getElementById('search-input').addEventListener('keyup', function(event) {
    if (event.key === 'Backspace' || event.key === 'Delete') {
        const keyword = this.value.trim();
        const suggestionBox = document.getElementById('suggestion-box');

        if (keyword.length === 0) {
            suggestionBox.innerHTML = ''; // 清空建議框
            suggestionBox.style.display = 'none';
        }
    }
});

// 點擊搜索框外部時清空建議框
document.addEventListener('click', function(event) {
    const searchInput = document.getElementById('search-input');
    const suggestionBox = document.getElementById('suggestion-box');

    if (!searchInput.contains(event.target) && !suggestionBox.contains(event.target)) {
        suggestionBox.innerHTML = '';
        suggestionBox.style.display = 'none';
    }
});

// search book
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