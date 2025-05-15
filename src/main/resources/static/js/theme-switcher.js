// Aplicar tema imediatamente durante o carregamento para evitar flash
(function() {
    const isDarkMode = localStorage.getItem('darkMode') === 'true';
    if (isDarkMode) {
        document.documentElement.classList.add('dark-mode');
    }
})();

document.addEventListener('DOMContentLoaded', function() {
    // Botão de alternância do tema
    const themeToggle = document.getElementById('theme-toggle');
    const themeIcon = document.getElementById('theme-icon');
    
    // Verifica se já existe uma preferência salva
    const isDarkMode = localStorage.getItem('darkMode') === 'true';
    
    // Aplica o tema inicial ao body
    if (isDarkMode) {
        document.body.classList.add('dark-mode');
        themeIcon.classList.remove('fa-moon');
        themeIcon.classList.add('fa-sun');
    }
    
    // Remover classe de preload e habilitar transições
    setTimeout(function() {
        document.documentElement.classList.remove('dark-mode-preload');
        document.documentElement.classList.add('transition-ready');
    }, 50);
    
    // Função para alternar o tema
    function toggleTheme() {
        if (document.body.classList.contains('dark-mode')) {
            // Mudar para tema claro
            document.documentElement.classList.remove('dark-mode');
            document.body.classList.remove('dark-mode');
            localStorage.setItem('darkMode', 'false');
            themeIcon.classList.remove('fa-sun');
            themeIcon.classList.add('fa-moon');
        } else {
            // Mudar para tema escuro
            document.documentElement.classList.add('dark-mode');
            document.body.classList.add('dark-mode');
            localStorage.setItem('darkMode', 'true');
            themeIcon.classList.remove('fa-moon');
            themeIcon.classList.add('fa-sun');
        }
        
        // Disparar evento personalizado para notificar outros scripts
        const event = new CustomEvent('themeChanged');
        document.dispatchEvent(event);
    }
    
    // Adiciona o evento de clique ao botão
    themeToggle.addEventListener('click', toggleTheme);
}); 