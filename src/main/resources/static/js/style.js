// 動態生成 Category 和 Profile 的邏輯

document.addEventListener("DOMContentLoaded", function() {
    // 動態加載分類
    fetch("/api/bookPage/categories")
        .then(response => response.json())
        .then(categories => {
            const categoryDropdown = document.getElementById('categoryDropdown');
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

                const subCategoryContainer = document.createElement('div');
                mainCategories[mainCategory].forEach(subCategory => {
                    const subCategoryDiv = document.createElement('div');
                    subCategoryDiv.classList.add('sub-category-item');
                    subCategoryDiv.innerText = subCategory;
                    subCategoryContainer.appendChild(subCategoryDiv);
                });

                mainCategoryDiv.appendChild(subCategoryContainer);
                categoryDropdown.appendChild(mainCategoryDiv);
            });
        })
        .catch(error => console.error("Error fetching categories:", error));

    // Profile Dropdown
    document.getElementById('header-3').addEventListener('mouseover', function() {
        document.getElementById('profileDropdown').style.display = 'block';
    });

    document.getElementById('header-3').addEventListener('mouseleave', function() {
        document.getElementById('profileDropdown').style.display = 'none';
    });
});

