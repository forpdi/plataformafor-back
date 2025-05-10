## Plataforma For open source para:

- **ForPDI**: criação, gerenciamento, acompanhamento e divulgação de Planos de Desenvolvimento Institucional (PDI).
- **ForRisco**: criação, gerenciamento, acompanhamento e divulgação de Planos de Gestão de  Riscos.

Para mais informações, acesse: [https://www.gov.br/mec/pt-br/plataformafor/](https://www.gov.br/mec/pt-br/plataformafor/)

## Instalação com Docker

Pré-requisito: tenha o **docker** e o **docker compose** instalados.
#### Estrutura do projeto
Faça o clone dos repositórios **plataformafor-back** e **plataformafor-front** disponíveis em: [https://github.com/forpdi](https://github.com/forpdi)

A estrutura do projeto deve ficar da seguinte forma:

plataforma-for/
├── plataformafor-back/
└── plataformafor-front/

#### Variáveis de ambiente do backend

Defina as seguintes variáveis de ambiente:
| Variável                    | descrição                                                          |
|-----------------------------|--------------------------------------------------------------------|
| DATABASE_ROOT_PASSWORD      | Senha do usuário root do banco de dados                            |
| DATABASE_USERNAME           | Nome de usuário da aplicação para o banco de dados                 |
| DATABASE_PASSWORD           | Senha do usuário da aplicação para o banco de dados                |
| DATABASE_NAME               | Nome do banco de dados                                             |
| DATABASE_HOST_PORT          | Host e porta do banco de dados (Ex: localhost:3306)                |
| RECAPTCHA_DISABLED          | Flag para habilitar/desabilitar o google recaptcha (true/false)    |
| RECAPTCHA_SECRET_KEY_V2     | Chave sercreta do google recaptcha                                 |
| AWS_CREDENTIALS_ACCESS_KEY  | Chave AWS para acesso ao S3                                        |
| AWS_CREDENTIALS_SECRET_KEY  | Chave secreta AWS para acesso ao S3                                |
| AWS_S3_PRIVATE_DOCS_NAME    | Nome do bucket AWS S3 para storage de arquivos                     |
| AWS_S3_REGION               | Região do AWS S3                                                   |
| CRYPT_KEY                   | Chave secreta de 16 bytes para criptografia (Ex: Dn9#dO-?2Xl3L~5@) |
| SECURITY_JWT_EXPIRATION_SEC | Quantidade de segundos para expiração do token de autenticação JWT |
| SECURITY_JWT_KEY            | Chave secreta JWT                                                  |
| SECURITY_REFRESH_JWT_KEY    | Chave secreta JWT para renovação do token de acesso                |
| SYS_FRONTENDURL             | URL base de acesso ao frontend                                     |

Para outras configurações, verfique o arquivo: 
*plataformafor-back/src/main/resources/application.yml*

#### Variáveis de ambiente do frontend
Crie o arquivo **.env.production** na raiz do **plataformafor-front** e defina as seguintes variáveis de ambiente (utilize o arquivo *plataformafor-front/.env* como modelo)

| Variável                     | descrição                                                          |
|----------------------------- |--------------------------------------------------------------------|
| REACT_APP_RECAPTCHA_KEY_V2   | Chave do google recaptcha                                          |
| REACT_APP_RECAPTCHA_DISABLED | Flag para habilitar/desabilitar o google recaptcha (true/false)    |

#### Subindo os serviços

Dentro do diretório **plataformafor-back**, rode o seguinte comando para subir o serviço do banco de dados com o docker compose:
```
docker-compose up -d database
```
Aguarde um momento até que a base de dados seja inicializada e rode os comandos para subir o backend e o frontend:
```
docker-compose up -d backend frontend
```

#### Primeiro acesso
A aplicação poderá ser acessada via browser na porta 80. 
Credencias do usuário padrão:
email: admin@forpdi.org
senha: 12345

## Stack de tecnologias utilizadas
-   Banco de dados MySQL 5.7
-   Backend Spring boot 2.7
-   Frontend web desacoplado:
    -   React.js
    -   Backbone.js
    -   Webpack
