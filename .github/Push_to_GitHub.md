# Guia: subir o projeto para o GitHub e ativar CI/CD

## 1) Criar repositório no GitHub
- Acesse https://github.com/new e crie um repo (ex.: `vinicius-oliveira/Todo-platform`).
- **Não** inicialize com README/License (vamos subir os arquivos locais).

## 2) No diretório raiz do projeto (onde está o `pom.xml` parent)
```bash
git init
git add .
git commit -m "feat: initial import (multi-module + WS + Feign + Kafka + VT)"
git branch -M main
git remote add origin git@github.com:<seu-usuario>/<seu-repo>.git
git push -u origin main
```

Se preferir HTTPS:
```bash
git remote remove origin || true
git remote add origin https://github.com/<seu-usuario>/<seu-repo>.git
git push -u origin main
```

## 3) Verificar CI
- Vá na aba **Actions** do repositório — o workflow **CI (Maven Multi-Module)** roda em `main` e PRs.
- Ele executa **build + testes** e publica os **JARs** como artefatos.

## 4) Criar um release (CD de artefatos)
Crie uma tag de versão e envie:
```bash
git tag v0.1.0
git push origin v0.1.0
```
Isso dispara **Release (Artifacts)**, que compila e publica os JARs na página de *Releases*.

## 5) (Opcional) Publicar imagens Docker no GHCR
- Podemos adicionar depois um workflow que usa **Jib** ou **Dockerfiles** para publicar `ghcr.io/<usuario>/<nome-imagem>`
- Requer `docker/login-action` com `secrets.GITHUB_TOKEN` e `permissions: packages: write`.
