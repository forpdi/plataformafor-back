#### Aviso

Para subir a Plataforma-For por completo utilize o docker-compose na raiz do projeto.

#### Construindo somente a imagem Docker do Backend
Para construir o projeto com o Docker, se faz necessário ter no mínimo a versão 18.06 ou superior. 

Pré condição para iniciar o container:
  - Ter a instância do database iniciada
  - Configurar o arquivo conf/docker.dev.properties com as propriedades de banco de dados.

Execute os seguintes comandos dentro da pasta backend-java/ :
 - docker build -t platfor-backend .
 - docker run -it --name platfor-backend -p 8080:8080 -p 8009:8009 platfor-backend

Pronto! Seu container com a aplicação referente ao backend esta disponivel.
Para testar acesse em: http://localhost:8080/forpdi/environment

### Atualização do repositório

Para atualizar o repositório do back-end é necessário substituir apenas o conteúdo da pasta `src` e os arquivos de properties na pasta `conf` caso seja adicionado novas configurações ao projeto.

Os comandos a seguir mostram o passo a passo da atualização da pasta `src`:

```sh
# 1. Criar pasta para os projetos
mkdir plat-fot

# 2. Clonar repositório do backend (via SSH)
git clone ssh://git@gitlab.rnp.br:2022/platfor/backend.git

# 3. Clonar repositório do Github (via HTTPS)
git clone https://github.com/forpdi/plataforma-for.git

# 4. Atualizando backend
rm -rf ./backend/src/*
cp -r ./plataforma-for/backend-java/src/* ./backend/src/

# 5. Versionando as alterações (Substituir COMMIT_MESSAGE)
git add . && git commit -m "COMMIT_MESSAGE"
git push
```
