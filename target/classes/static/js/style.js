// Simulate categories and subcategories
const subCategories = {
    'Category 1': ['Sub 1-1', 'Sub 1-2', 'Sub 1-3'],
    'Category 2': ['Sub 2-1', 'Sub 2-2', 'Sub 2-3'],
    'Category 3': ['Sub 3-1', 'Sub 3-2', 'Sub 3-3'],
};

document.querySelectorAll('.category-item').forEach(item => {
    item.addEventListener('mouseover', function() {
        const categoryName = this.getAttribute('data-category');
        const subCategoryBox = document.getElementById('categorySub');
        subCategoryBox.innerHTML = ''; // Clear previous subcategories
        subCategories[categoryName].forEach(sub => {
            const div = document.createElement('div');
            div.classList.add('category-item');
            div.innerText = sub;
            subCategoryBox.appendChild(div);
        });
        subCategoryBox.style.display = 'block'; // Show subcategory box
    });
});

document.getElementById('categoryDropdown').addEventListener('mouseleave', function() {
    document.getElementById('categorySub').style.display = 'none'; // Hide subcategories when mouse leaves
});

document.getElementById('profileBtn').addEventListener('click', function() {
    // Here you can redirect to the profile page if necessary.
});
