provider "aws" {
  region                      = "us-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_requesting_account_id  = true
  skip_metadata_api_check     = true

  endpoints {
    ecs  = "http://localhost:4566"
    ecr  = "http://localhost:4566"
    logs = "http://localhost:4566"
  }
}

# 1. Repositorio ECR Local (Renombrado)
resource "aws_ecr_repository" "api_repo" {
  name = "login-api-repo"
}

# 2. Clúster de ECS emulado
resource "aws_ecs_cluster" "main_cluster" {
  name = "superapp-local-cluster"
}

# 3. Task Definition (Actualizado con el nuevo nombre de imagen)
resource "aws_ecs_task_definition" "api_task" {
  family                   = "login-api-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"

  container_definitions = jsonencode([
    {
      name      = "login-api"
      # --- CAMBIO AQUÍ ---
      image     = "login-api:latest"
      # -------------------
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      environment = [
        { name = "DB_URL", value = "jdbc:mariadb://host.docker.internal:3306/colombia_db" },
        { name = "DB_USERNAME", value = "root" },
        { name = "DB_PASSWORD", value = "123456" },
        { name = "JWT_SECRET", value = "una_clave_larga_y_segura_de_al_menos_32_bytes" },
        { name = "JWT_EXPIRATION", value = "3600000" },
        { name = "JWT_REFRESH_TOKEN_EXPIRATION", value = "604800000" }
      ]
    }
  ])
}

# 4. Servicio de ECS corriendo sobre Fargate
resource "aws_ecs_service" "api_service" {
  name            = "login-api-service"
  cluster         = aws_ecs_cluster.main_cluster.id
  task_definition = aws_ecs_task_definition.api_task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = ["subnet-12345678"]
    security_groups  = ["sg-12345678"]
    assign_public_ip = true
  }
}