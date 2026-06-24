const cartFeedback = document.getElementById('cartFeedback');
const cartFeedbackMessage = document.getElementById('cartFeedbackMessage');

function showCartFeedback(type, message) {
    if (!cartFeedback || !cartFeedbackMessage) {
        return;
    }

    cartFeedback.classList.remove('hidden', 'info', 'success', 'warning', 'error');
    cartFeedback.classList.add(type);
    cartFeedbackMessage.textContent = message;

    requestAnimationFrame(() => {
        const rootStyles = getComputedStyle(document.documentElement);
        const navbarHeight = parseFloat(rootStyles.getPropertyValue('--navbar-height')) || 72;
        const categoriesHeight = document.querySelector('.categories-section')?.offsetHeight || 0;
        const targetTop = cartFeedback.getBoundingClientRect().top + window.scrollY;

        window.scrollTo({
            top: Math.max(targetTop - navbarHeight - categoriesHeight - 16, 0),
            behavior: 'smooth'
        });
    });
}

document.querySelectorAll('.login-required-button').forEach(button => {
    button.addEventListener('click', () => {
        showCartFeedback('info', 'Login to add products to your cart.');
    });
});
