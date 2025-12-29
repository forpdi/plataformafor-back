# Plataforma For – ForPDI / ForRisco

A **Plataforma For** é composta por dois módulos principais:

- **ForPDI**: criação, gerenciamento, acompanhamento e divulgação de Planos de Desenvolvimento Institucional (PDI).
- **ForRisco**: criação, gerenciamento, acompanhamento e divulgação de Planos de Gestão de Riscos.

Para mais informações institucionais, acesse:
👉 https://www.gov.br/mec/pt-br/plataformafor/

---

## 1. Instalação com Docker

> **Pré-requisito:** ter **Docker** e **Docker Compose** instalados e funcionando.

### 1.1. Estrutura do projeto

Clone os repositórios **plataformafor-back** e **plataformafor-front** a partir da organização oficial:

```bash
git clone https://github.com/forpdi/plataformafor-back.git
git clone https://github.com/forpdi/plataformafor-front.git
```

Estrutura desejada:

```text
plataforma-for/
├── plataformafor-back/
└── plataformafor-front/
```

---

## 2. Configuração de variáveis de ambiente

### 2.1. Backend (`plataformafor-back/.env`)

Defina as seguintes variáveis de ambiente:

| Variável                      | Descrição                                                          |
|-------------------------------|--------------------------------------------------------------------|
| `DATABASE_ROOT_PASSWORD`      | Senha do usuário root do banco de dados                           |
| `DATABASE_USERNAME`           | Nome de usuário da aplicação para o banco de dados                |
| `DATABASE_PASSWORD`           | Senha do usuário da aplicação para o banco de dados               |
| `DATABASE_NAME`               | Nome do banco de dados                                            |
| `DATABASE_HOST_PORT`          | Host e porta do banco de dados (Ex.: `database:3306`)             |
| `RECAPTCHA_DISABLED`          | Habilita/desabilita o Google reCAPTCHA (`true`/`false`)           |
| `RECAPTCHA_SECRET_KEY_V2`     | Chave secreta do Google reCAPTCHA v2                              |
| `AWS_CREDENTIALS_ACCESS_KEY`  | Chave AWS para acesso ao S3                                       |
| `AWS_CREDENTIALS_SECRET_KEY`  | Chave secreta AWS para acesso ao S3                               |
| `AWS_S3_PRIVATE_DOCS_NAME`    | Nome do bucket S3 para storage de arquivos                        |
| `AWS_S3_REGION`               | Região do S3 (ex.: `us-east-1`)                                   |
| `CRYPT_KEY`                   | Chave de 16 bytes para criptografia (ex.: `Dn9#dO-?2Xl3L~5@`)     |
| `SECURITY_JWT_EXPIRATION_SEC` | Expiração do token JWT em segundos                                |
| `SECURITY_JWT_KEY`            | Chave secreta JWT (assinatura do token)                           |
| `SECURITY_REFRESH_JWT_KEY`    | Chave secreta JWT para refresh token                              |
| `SYS_FRONTENDURL`             | URL base de acesso ao frontend (ex.: `http://localhost`)          |

> Para outros parâmetros avançados, consulte
> `plataformafor-back/src/main/resources/application.yml`.

---

### 2.2. Frontend (`plataformafor-front/.env.production`)

Crie o arquivo **`.env.production`** na raiz do `plataformafor-front` (use `plataformafor-front/.env` como modelo) e defina:

| Variável                       | Descrição                                                          |
|--------------------------------|--------------------------------------------------------------------|
| `REACT_APP_RECAPTCHA_KEY_V2`   | Chave pública do Google reCAPTCHA                                 |
| `REACT_APP_RECAPTCHA_DISABLED` | Habilita/desabilita o Google reCAPTCHA (`true`/`false`)           |

---

## 3. Subindo os serviços

Dentro do diretório **`plataformafor-back`**, execute:

1. Subir apenas o banco de dados (primeira execução):

   ```bash
   docker-compose up -d database
   ```

   Aguarde alguns segundos até que o MySQL esteja inicializado.

2. Subir backend e frontend:

   ```bash
   docker-compose up -d backend frontend
   ```

---

## 4. Primeiro acesso

- A aplicação, por padrão, será exposta na porta **80** do servidor.
- Acesse via navegador:

  ```text
  http://SEU_HOST/forpdi
  ```

- Credenciais do usuário padrão:

  ```text
  e-mail: admin@forpdi.org
  senha:  12345
  ```

> **Importante:** altere a senha do usuário administrador no primeiro acesso.

---

## 5. Stack de tecnologias utilizadas

- **Banco de dados:** MySQL 5.7
- **Backend:** Spring Boot 3.5
- **Frontend web desacoplado:**
  - React.js
  - Backbone.js
  - Webpack

---

## 6. Manual Técnico de Instalação (resumo)

Para uma instalação mais detalhada, recomenda-se:

- **Pré-requisitos mínimos:**
  - Docker >= 24 (`docker --version`)
  - Docker Compose v2 (`docker compose version`)
  - Portas livres: 80/443 (frontend), 8080 (backend), 3306 (MySQL).
  - Recursos de servidor: 4 GB RAM livre, 10 GB de disco disponível.
  - Repositórios `plataformafor-back/` e `plataformafor-front/` clonados lado a lado.
  - Usuário com permissão para executar Docker (ou uso de `sudo`).

- **Clonagem:**

  ```bash
  git clone https://github.com/forpdi/plataformafor-back.git
  git clone https://github.com/forpdi/plataformafor-front.git
  ```

- **Arquivo `.env` no backend (exemplo mínimo):**

  ```env
  DATABASE_ROOT_PASSWORD=senhaRoot
  DATABASE_USERNAME=forpdi
  DATABASE_PASSWORD=forpdi
  DATABASE_NAME=forpdi_db
  DATABASE_HOST_PORT=database:3306
  RECAPTCHA_DISABLED=true
  CRYPT_KEY=Dn9#dO-?2Xl3L~5@
  SECURITY_JWT_EXPIRATION_SEC=3600
  SECURITY_JWT_KEY=minhaChaveJwt
  SECURITY_REFRESH_JWT_KEY=minhaChaveRefresh
  SYS_FRONTENDURL=http://localhost
  ```

- **Arquivo `.env.production` no frontend (exemplo):**

  ```env
  REACT_APP_RECAPTCHA_KEY_V2=sua-chave
  REACT_APP_RECAPTCHA_DISABLED=true
  ```

- **Build e subida (primeira vez ou após mudanças importantes):**

  ```bash
  cd plataformafor-back
  docker-compose up -d --build
  ```

- **Validações iniciais:**
  - `docker-compose ps` → serviços em `Up`.
  - Logs do banco e backend sem erros.
  - Acesso via browser ao `/forpdi`.

---

## 7. Backup e Recuperação (resumo)

> Para detalhes completos, consulte o **Manual de Backup, Recuperação e Continuidade da Plataforma For**.

- Recomenda-se:
  - Manter **rotina de backup diário** do banco `forpdi_db`.
  - Armazenar os arquivos de backup em local seguro (storage de rede, S3, etc.).
  - Realizar testes periódicos de restauração em ambiente de homologação.

- Método oficial sugerido:
  - **Backup manual via DBeaver**, executado por administrador autorizado.
  - Registrar data, responsável e local de armazenamento de cada backup.

---

## 8. Guia de Atualização de Versão (resumo)

- **Antes de atualizar:**
  - Ler changelog/release notes.
  - Garantir **backup recente** válido.
  - Validar novas variáveis de ambiente (se houver).

- **Atualização típica (código local):**

  ```bash
  # Atualizar backend
  cd plataformafor-back
  git pull

  # Atualizar frontend
  cd ../plataformafor-front
  git pull

  # Voltar ao backend e recriar containers
  cd ../plataformafor-back
  docker-compose up -d --build
  ```

- **Validações pós-deploy:**
  - Backend inicializado (`Started Application` nos logs).
  - Login funcionando.
  - Fluxos principais testados (consultas, cadastros, etc.).

---

## 9. Documentação Operacional (TI – Visão Simplificada)

- **Subir tudo:**

  ```bash
  cd /opt/plataforma-for/plataformafor-back
  docker-compose up -d
  ```

- **Parar tudo:**

  ```bash
  docker-compose down
  ```

- **Reiniciar apenas backend e frontend:**

  ```bash
  docker-compose restart backend frontend
  ```

- **Verificar status:**

  ```bash
  docker-compose ps
  ```

- **Ver logs do backend:**

  ```bash
  docker-compose logs -f backend
  ```

- **Quando acionar suporte técnico:**
  - Sistema fora do ar mesmo após restart.
  - Erros de banco de dados persistentes.
  - Suspeita de perda de dados ou incidente de segurança.
