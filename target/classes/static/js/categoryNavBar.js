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
