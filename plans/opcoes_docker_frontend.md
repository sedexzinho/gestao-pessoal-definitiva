# Opções para Rodar o Frontend via Docker

## Visão Geral

Atualmente você tem:
- **Banco de dados**: PostgreSQL rodando via Docker (docker-compose.yml)
- **Backend**: Java/Spring Boot rodando localmente na porta 8080
- **Frontend**: React/Vite rodando localmente na porta 5173

## Opções de Containerização do Frontend

### Opção 1: Criar Dockerfile para o Frontend

**O que é**: Criar um Dockerfile para empacotar o frontend React em um container Docker.

**Como funciona**:
1. O código React é buildado (gerando arquivos estáticos)
2. Esses arquivos são servidos por um servidor web (nginx ou similar)
3. O container Docker expõe uma porta para acesso

**Vantagens**:
- Frontend totalmente isolado em container
- Ambiente reproduzível e consistente
- Fácil deployment em qualquer ambiente com Docker

**Desvantagens**:
- Necessário rebuild a cada mudança no código
- Curva de aprendizado se não conhece Docker/nginx
- Mais complexidade na configuração inicial

**Comandos básicos**:
```bash
# Build da aplicação
npm run build

# Build da imagem Docker
docker build -t gestao-pessoal-frontend .

# Rodar o container
docker run -p 80:80 gestao-pessoal-frontend
```

---

### Opção 2: Configurar docker-compose para Frontend + Banco

**O que é**: Adicionar o frontend ao docker-compose.yml existente, junto com o banco de dados.

**Como funciona**:
- O docker-compose orchestra múltiplos serviços
- Você pode ter: postgres + frontend (usando nginx)
- Todos os serviços sobem com um único comando

**Vantagens**:
- Gerenciamento centralizado de todos os serviços
-um comando para subir tudo
- Redes internas entre containers

**Estrutura exemplo**:
```yaml
services:
  postgres:
    image: postgres:15
    container_name: gestao-pessoal-db
    environment:
      POSTGRES_DB: minhagestao
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: "4546"
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  frontend:
    build: ./gestao-pessoal
    ports:
      - "80:80"
    depends_on:
      - postgres

volumes:
  postgres_data:
```

**Desvantagens**:
- build pode demorar mais
- Configuração de proxy para API precisa ser ajustada

---

### Opção 3: Frontend já conecta ao Backend no Docker ( atual )

**O que é**: Manter o frontend rodando localmente (npm run dev), mas conectando-se ao banco de dados que está no Docker.

**O que já funciona**:
- Frontend (localhost:5173) → Backend (localhost:8080) → Banco Docker (localhost:5433)
- A API Java já está configurada para usar variáveis de ambiente (application.properties usa ${DB_URL})

**Vantagens**:
- Desenvolvimento rápido (hot reload do React)
- Sem necessidade de containerizar o frontend
- Mais simples de debugar

**Desvantagens**:
- Ambiente local depende de software instalado (Node.js)
- Não é 100% containerizado

**Configuração atual**:
- Banco: Docker na porta 5433
- Backend: local na porta 8080
- Frontend: local na porta 5173

---

### Opção 4: Arquitetura Completa (Recomendada para Produção)

**O que é**: Containerizar todos os componentes: Banco + Backend + Frontend.

**Arquitetura completa**:

```
┌─────────────────────────────────────────────┐
│              Docker Network                  │
│  ┌──────────────┐  ┌──────────────────┐    │
│  │   PostgreSQL │  │  Java Backend   │    │
│  │   (Docker)   │──│  (Docker)       │    │
│  │   porta 5432 │  │  porta 8080     │    │
│  └──────────────┘  └────────┬─────────┘    │
│                             │               │
│                    ┌────────▼─────────┐     │
│                    │   Nginx/Frontend │     │
│                    │   (Docker)       │     │
│                    │   porta 80/443   │     │
│                    └──────────────────┘     │
└─────────────────────────────────────────────┘
```

**Vantagens**:
- Ambiente 100% containerizado
- Fácil replicação em diferentes máquinas
- Ideal para produção e CI/CD
- Todas as dependências isoladas

**Desvantagens**:
- Maior complexidade inicial
- Necessita configurar rede entre containers
- Debug mais complexo

**docker-compose.yml completo**:
```yaml
services:
  postgres:
    image: postgres:15
    container_name: gestao-pessoal-db
    environment:
      POSTGRES_DB: minhagestao
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: "4546"
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - gestao-network

  backend:
    build: .
    container_name: gestao-pessoal-backend
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/minhagestao
      DB_USERNAME: postgres
      DB_PASSWORD: "4546"
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    networks:
      - gestao-network

  frontend:
    build: ./gestao-pessoal
    container_name: gestao-pessoal-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - gestao-network

volumes:
  postgres_data:

networks:
  gestao-network:
    driver: bridge
```

---

## Recomendação

Para o seu caso específico (desenvolvimento local):

| Cenário | Recomendação |
|---------|--------------|
| Desenvolvimento rápido com hot reload | Opção 3 (manter frontend local) |
| Precisa apresentar/demo para alguém | Opção 2 (docker-compose simples) |
| Ambiente de produção | Opção 4 (arquitetura completa) |
| Apenas testar containerização | Opção 1 (apenas Dockerfile) |

---

## Próximos Passos

Se quiser implementar alguma dessas opções, posso:

1. Criar o **Dockerfile** para o frontend React
2. Atualizar o **docker-compose.yml** para incluir o frontend
3. Criar a **arquitetura completa** com todos os serviços
4. Ajustar a **configuração de proxy** para ambiente Docker

Qual opção você gostaria de implementar?
