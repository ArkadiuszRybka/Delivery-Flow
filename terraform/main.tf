module "vpc" {
  source = "./modules/vpc"

  project_name = var.project_name
}

module "rds" {
  source = "./modules/rds"

  project_name       = var.project_name
  vpc_id             = module.vpc.vpc_id
  vpc_cidr           = module.vpc.vpc_cidr
  private_subnet_ids = module.vpc.private_subnet_ids
}

module "elasticache" {
  source = "./modules/elasticache"

  project_name       = var.project_name
  vpc_id             = module.vpc.vpc_id
  vpc_cidr           = module.vpc.vpc_cidr
  private_subnet_ids = module.vpc.private_subnet_ids
}

module "msk" {
  source = "./modules/msk"

  project_name       = var.project_name
  vpc_id             = module.vpc.vpc_id
  vpc_cidr           = module.vpc.vpc_cidr
  private_subnet_ids = module.vpc.private_subnet_ids
}

module "eks" {
  source = "./modules/eks"

  project_name        = var.project_name
  vpc_id              = module.vpc.vpc_id
  private_subnet_ids  = module.vpc.private_subnet_ids
  public_subnet_ids   = module.vpc.public_subnet_ids
}
