const cartFeedback = document.getElementById('cartFeedback');
const cartFeedbackMessage = document.getElementById('cartFeedbackMessage');

function showCartFeedback(type, message) {
    cartFeedback.classList.remove('hidden', 'info', 'success', 'warning', 'error');
    cartFeedback.classList.add(type);
    cartFeedbackMessage.textContent = message;
    cartFeedback.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

document.querySelectorAll('.login-required-button').forEach(button => {
    button.addEventListener('click', () => {
        showCartFeedback('info', 'Login to add products to your cart.');
    });
});