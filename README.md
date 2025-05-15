# Personal Blog

Um blog pessoal completo desenvolvido com Spring Boot, Thymeleaf e MySQL.

## Características

- Design responsivo com tema claro/escuro
- Painel administrativo protegido
- Editor de texto rico (TinyMCE)
- Upload de imagens e mídia
- Suporte para edição e publicação de posts
- Página "Sobre Mim" personalizável

## Tecnologias

- **Backend**: Spring Boot, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Banco de Dados**: MySQL
- **Ferramentas**: Maven, Git

## Começando

### Pré-requisitos
- JDK 17+
- Maven
- MySQL

### Instalação

1. Clone o repositório:
   ```bash
   git clone https://github.com/gabslee/personal-blog.git
   cd personal-blog
   ```

2. Configure o banco de dados no arquivo `src/main/resources/application.properties`

3. Execute a aplicação:
   ```bash
   mvn spring-boot:run
   ```

4. Acesse o aplicativo em [http://localhost:8080](http://localhost:8080)

## Uso

- **Login Admin**: Acesse /login para entrar no painel administrativo
- **Dashboard**: Gerencie seus posts, perfil e conteúdo
- **Editor**: Crie e edite posts com o editor TinyMCE

## Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## Autor

Gabriel Menezes dos Anjos - [GitHub](https://github.com/gabslee) 