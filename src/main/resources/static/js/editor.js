document.addEventListener('DOMContentLoaded', function() {
    initTinyMCE();
    
    // Reinicializar o editor quando o tema mudar
    document.addEventListener('themeChanged', function() {
        // Primeiro precisamos destruir a instância atual
        if (tinymce.activeEditor) {
            tinymce.activeEditor.remove();
        }
        // Depois reiniciamos com a nova configuração de tema
        setTimeout(initTinyMCE, 100);
    });
    
    // Função para inicializar o TinyMCE
    function initTinyMCE() {
        const isDarkMode = document.documentElement.classList.contains('dark-mode');
        const contentCss = isDarkMode 
            ? '/css/dark-theme.css' 
            : ['https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&display=swap', '/css/dark-theme.css'];
        
        const skin = isDarkMode ? 'oxide-dark' : 'oxide';
        const contentStyle = isDarkMode
            ? 'body { font-family: Roboto, sans-serif; color: #e0e0e0; background-color: #252525; padding: 20px; } ' + 
              'img { max-width: 100%; height: auto; display: block; margin: 0 auto; } ' +
              'figure { margin: 1em 0; } ' + 
              'figure img { margin-bottom: 0.5em; } ' +
              'figcaption { font-size: 0.9em; color: #a0a0a0; text-align: center; }'
            : 'body { font-family: Roboto, sans-serif; color: #333; background-color: #fff; padding: 20px; } ' + 
              'img { max-width: 100%; height: auto; display: block; margin: 0 auto; } ' +
              'figure { margin: 1em 0; } ' + 
              'figure img { margin-bottom: 0.5em; } ' +
              'figcaption { font-size: 0.9em; color: #666; text-align: center; }';
            
        // Obter os tokens CSRF
        const csrfTokenName = document.querySelector('meta[name="_csrf_parameter"]')?.content || '_csrf';
        const csrfTokenValue = document.querySelector('meta[name="_csrf"]')?.content || '';
            
        tinymce.init({
            selector: '.rich-editor',
            skin: skin,
            content_css: contentCss,
            content_style: contentStyle,
            height: 550,
            menubar: true,
            branding: false,
            promotion: false,
            
            // Configurações adicionais para melhorar usabilidade
            resize: true, // Permite redimensionar o editor
            autoresize_bottom_margin: 50,
            paste_data_images: true, // Permite colar imagens
            paste_as_text: false,
            browser_spellcheck: true,
            object_resizing: true,
            
            // Plugins
            plugins: [
                'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
                'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
                'insertdatetime', 'media', 'table', 'help', 'wordcount', 'emoticons',
                'codesample', 'hr', 'nonbreaking', 'pagebreak', 'quickbars', 'autoresize'
            ],
            
            // Barra de ferramentas
            toolbar: [
                'undo redo | formatselect | bold italic underline strikethrough',
                'alignleft aligncenter alignright alignjustify | outdent indent | numlist bullist',
                'link image media emoticons | table codesample | fullscreen code'
            ],
            
            // Principais formatos de estilo
            style_formats: [
                { title: 'Heading 1', format: 'h1' },
                { title: 'Heading 2', format: 'h2' },
                { title: 'Heading 3', format: 'h3' },
                { title: 'Paragraph', format: 'p' },
                { title: 'Blockquote', format: 'blockquote' },
                { title: 'Code', inline: 'code' },
                {
                    title: 'Media Container',
                    block: 'div',
                    classes: 'media-container',
                    wrapper: true
                }
            ],
            
            // Quickbars e contexto
            quickbars_selection_toolbar: 'bold italic | h2 h3 | blockquote quicklink',
            quickbars_insert_toolbar: 'image media codesample',
            contextmenu: 'link image table',
            
            // Configurações de salvamento e autosave
            autosave_ask_before_unload: true,
            
            // Configurações de imagem
            image_advtab: true,
            image_uploadtab: true,
            image_dimensions: true,
            image_title: true,
            image_caption: true,
            image_class_list: [
                { title: 'None', value: '' },
                { title: 'Responsive Image', value: 'img-fluid' }
            ],
            
            // Configurações de média
            media_dimensions: true,
            media_live_embeds: true,
            media_alt_source: false,
            media_poster: false,
            
            // Configurações de upload
            automatic_uploads: true,
            images_upload_url: '/admin/upload-image',
            images_reuse_filename: true,
            file_picker_types: 'image media',
            
            // Manipulador de upload personalizado para incluir o token CSRF
            images_upload_handler: function(blobInfo, progress) {
                return handleImageUpload(blobInfo, progress, csrfTokenName, csrfTokenValue);
            },
            
            // Seletor de arquivo personalizado
            file_picker_callback: function(callback, value, meta) {
                // Mostrar o seletor de arquivo apenas para imagens e mídia
                if (meta.filetype === 'image' || meta.filetype === 'media') {
                    const input = document.createElement('input');
                    input.setAttribute('type', 'file');
                    
                    if (meta.filetype === 'image') {
                        input.setAttribute('accept', 'image/*');
                    } else if (meta.filetype === 'media') {
                        input.setAttribute('accept', 'video/*,audio/*');
                    }
                    
                    input.onchange = function() {
                        if (this.files && this.files[0]) {
                            const file = this.files[0];
                            
                            // Criar um blob info para o upload
                            const blobInfoId = 'blobid' + (new Date()).getTime();
                            const blobInfo = new tinymce.file.BlobInfo({
                                id: blobInfoId,
                                blob: file,
                                base64: function() { return ''; },
                                filename: function() { return file.name; }
                            });
                            
                            // Fazer o upload
                            handleImageUpload(blobInfo, function(progress) {
                                console.log('Upload progress: ' + progress + '%');
                            }, csrfTokenName, csrfTokenValue).then(function(url) {
                                // Chamar o callback com a URL
                                callback(url, { title: file.name });
                            }).catch(function(error) {
                                console.error('Error uploading file:', error);
                                alert('Error uploading file: ' + error);
                            });
                        }
                    };
                    
                    input.click();
                }
            },
            
            setup: function(editor) {
                // Ajustar UI quando o editor carregar
                editor.on('init', function() {
                    applyEditorStyles(editor, isDarkMode);
                });
                
                // Fix para o cursor desaparecer após a inserção de mídia
                editor.on('NodeChange', function(e) {
                    if (e.element.nodeName === 'IMG' || e.element.nodeName === 'IFRAME') {
                        // Forçar o focus no editor após inserir mídia
                        setTimeout(function() {
                            editor.focus();
                        }, 100);
                    }
                });
                
                // Adicionar atalhos personalizados
                editor.addShortcut('Meta+S', 'Save', function() {
                    document.querySelector('form.editor-form .save-btn').click();
                });
                
                // Adicionar indicador de contagem de palavras na barra de status
                editor.on('init', function() {
                    const statusbar = editor.getContainer().querySelector('.tox-statusbar');
                    if (statusbar) {
                        const wordCountEl = document.createElement('div');
                        wordCountEl.className = 'tox-statusbar__wordcount';
                        wordCountEl.innerHTML = '<span>0 words</span>';
                        statusbar.insertBefore(wordCountEl, statusbar.firstChild);
                        
                        editor.on('KeyUp', updateWordCount);
                        editor.on('Change', updateWordCount);
                        
                        function updateWordCount() {
                            const count = editor.plugins.wordcount.getCount();
                            wordCountEl.innerHTML = `<span>${count} words</span>`;
                        }
                    }
                });
            }
        });
    }
    
    // Função para fazer upload de imagens
    function handleImageUpload(blobInfo, progress, csrfTokenName, csrfTokenValue) {
        return new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.withCredentials = false;
            
            xhr.upload.onprogress = (e) => {
                progress(e.loaded / e.total * 100);
            };
            
            xhr.onload = function() {
                if (xhr.status === 403) {
                    reject({ message: 'HTTP Error: ' + xhr.status, remove: true });
                    return;
                }
                
                if (xhr.status < 200 || xhr.status >= 300) {
                    reject('HTTP Error: ' + xhr.status);
                    return;
                }
                
                try {
                    const json = JSON.parse(xhr.responseText);
                    
                    if (!json || !json.location) {
                        reject('Invalid JSON response');
                        return;
                    }
                    
                    resolve(json.location);
                } catch (e) {
                    reject('Invalid JSON: ' + e.message);
                }
            };
            
            xhr.onerror = function() {
                reject('Image upload failed');
            };
            
            const formData = new FormData();
            formData.append('file', blobInfo.blob(), blobInfo.filename());
            
            // Adicionar o token CSRF se disponível
            if (csrfTokenName && csrfTokenValue) {
                formData.append(csrfTokenName, csrfTokenValue);
            }
            
            xhr.open('POST', '/admin/upload-image');
            xhr.send(formData);
        });
    }
    
    // Personalizar estilos do editor para combinar com o tema do site
    function applyEditorStyles(editor, isDarkMode) {
        const editorContainer = editor.getContainer();
        
        if (!editorContainer) return;
        
        // Adicionar classe para estilizar conforme tema
        editorContainer.classList.add('custom-tinymce-editor');
        
        if (isDarkMode) {
            editorContainer.classList.add('dark-theme-editor');
        } else {
            editorContainer.classList.remove('dark-theme-editor');
        }
        
        // Suavizar as bordas e cantos do editor
        editorContainer.style.borderRadius = '8px';
        editorContainer.style.overflow = 'hidden';
        editorContainer.style.border = isDarkMode ? '1px solid #333' : '1px solid #e0e0e0';
        editorContainer.style.boxShadow = isDarkMode 
            ? '0 2px 12px rgba(0, 0, 0, 0.2)' 
            : '0 2px 12px rgba(0, 0, 0, 0.05)';
            
        // Adicionar feedback visual nos botões ao passar o mouse
        const buttons = editorContainer.querySelectorAll('.tox-tbtn');
        buttons.forEach(btn => {
            btn.addEventListener('mouseenter', function() {
                this.style.transition = 'all 0.2s ease';
                this.style.transform = 'translateY(-1px)';
            });
            
            btn.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
            });
        });
        
        // Garantir que pop-ups apareçam acima de tudo
        const body = document.body;
        const toxDialog = document.querySelector('.tox-tinymce-aux');
        if (toxDialog) {
            body.appendChild(toxDialog);
        }
    }
}); 