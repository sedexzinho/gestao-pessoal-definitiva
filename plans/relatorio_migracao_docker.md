# Relatório de Migração do Banco de Dados para Docker

## Problema Inicial

O projeto tinha um banco de dados PostgreSQL local instalado na máquina (porta 5432) e um banco Docker configurado inicialmente com configurações diferentes. O objetivo era migrar os dados do banco local para o Docker.

## O que foi identificado

### Configurações encontradas:
- **Banco local**: PostgreSQL 18, porta 5432, banco `MinhaGestao`, senha `4546`
- **Banco Docker original**: PostgreSQL 15, porta 5432, banco `gestao_pessoal`, senha `postgres`
- **Conflito**: Ambos queriam usar a mesma porta 5432

## Passos Executados

### 1. Identificação dos processos
```
docker ps -a
```
Encontramos:
- Container `peaceful_goldwasser`: PostgreSQL 15 rodando na porta 5433
- Container `gestao-pessoal-db`: PostgreSQL 18 que não iniciava (problema de compatibilidade)
- PostgreSQL local na porta 5432 (PID 8516)

### 2. Decisão de usar o container existente
Decidimos usar o container `peaceful_gredwasser` que já estava rodando PostgreSQL 15 na porta 5433.

### 3. Backup do banco local
```bash
set PGPASSWORD=4546
"C:\Program Files\PostgreSQL\18\bin\pg_dump.exe" -h localhost -p 5432 -U postgres -d MinhaGestao > backup.sql
```

### 4. Criação do banco no Docker
```bash
docker exec -i peaceful_goldwasser psql -U postgres -c "CREATE DATABASE "MinhaGestao";"
```
Nota: O PostgreSQL criou com nome minúsculo `minhagestao`.

### 5. Restauração do backup no Docker
```bash
docker cp backup.sql peaceful_goldwasser:/tmp/backup.sql
docker exec -i peaceful_goldwasser psql -U postgres -d minhagestao -f /tmp/backup.sql
```

### 6. Atualização das configurações

#### Arquivo `.env`:
```env
DB_URL=jdbc:postgresql://localhost:5433/minhagestao
DB_USERNAME=postgres
DB_PASSWORD=4546
```

#### Arquivo `docker-compose.yml`:
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

volumes:
  postgres_data:
```

## Dados Migrados

Verificação dos dados no Docker:
```
docker exec -i peaceful_goldwasser psql -U postgres -d minhagestao -c "\dt"
```

Tabelas encontradas:
- `users` - 1 registro
- `categories` - 3 registros
- `expenses` - 8 registros
- `revenues` - 1 registro

## Como usar agora

### Rodar a aplicação Java:
```bash
mvn spring-boot:run
```

### Acessar o banco via terminal:
```bash
docker exec -it peaceful_goldwasser psql -U postgres -d minhagestao
```

### Comandos úteis dentro do psql:
```sql
-- Ver todas as tabelas
\dt

-- Ver dados de despesas
SELECT * FROM expenses;

-- Ver dados de receitas
SELECT * FROM revenues;

-- Ver usuários
SELECT * FROM users;

-- Ver categorias
SELECT * FROM categories;

-- Sair
\q
```

### Conexão via DBeaver/pgAdmin:
- **Host**: localhost
- **Port**: 5433
- **Database**: minhagestao
- **Username**: postgres
- **Password**: 4546

## Arquivos Modificados

1. `.env` - Atualizado com nova URL e credenciais
2. `docker-compose.yml` - Atualizado com configurações corretas

## Notas Importantes

1. **Porta 5433**: O Docker usa a porta 5433 para evitar conflito com o PostgreSQL local na 5432
2. **Nome do banco**: O banco foi criado como `minhagestao` (minúsculo) no Docker
3. **Versão PostgreSQL**: Docker usa PostgreSQL 15, local era 18
4. **Container em uso**: O container `peaceful_goldwasser` foi usado em vez de criar um novo
