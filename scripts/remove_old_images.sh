#!/bin/bash

echo "================================================================================"
echo "  ATENÇÃO: ESTE SCRIPT É PARA USO EXCLUSIVO NO AMBIENTE DE DESENVOLVIMENTO."
echo "  É RESTRITAMENTE PROIBIDO UTILIZÁ-LO EM AMBIENTES DE PRODUÇÃO."
echo "  QUALQUER USO NÃO AUTORIZADO PODE CAUSAR PERDA DE DADOS E/OU INTERFERÊNCIA NO AMBIENTE DE PRODUÇÃO."
echo "================================================================================"

# Defina o número máximo de imagens que você deseja manter
MAX_IMAGES=1

# Nome do repositório que você deseja remover imagens antigas
IMAGE_NAME="vitormob/website_api"

# Liste as imagens Docker do repositório específico, ordenadas por data de criação, excluindo as mais recentes
IMAGES_TO_REMOVE=$(docker images --format "{{.CreatedSince}} {{.Repository}}:{{.Tag}} {{.ID}}" \
| grep "$IMAGE_NAME" \
| sort -rk1 \
| tail -n +$((MAX_IMAGES + 1)) \
| awk '{print $3}')

# Verifique se há imagens para remover
if [ -z "$IMAGES_TO_REMOVE" ]; then
  echo "Nenhuma imagem antiga encontrada para remoção."
else
  echo "Removendo as seguintes imagens antigas:"
  echo "$IMAGES_TO_REMOVE"

  # Remova as imagens antigas
  for IMAGE_ID in $IMAGES_TO_REMOVE; do
    docker rmi $IMAGE_ID
    if [ $? -eq 0 ]; then
      echo "Imagem $IMAGE_ID removida com sucesso."
    else
      echo "Falha ao remover a imagem $IMAGE_ID."
    fi
  done
fi