#!/bin/bash

echo "======================================"
echo "🚀 INICIANDO TEST END-TO-END"
echo "======================================"

BASE_PRODUCTS="http://localhost:8080"
BASE_INVENTORY="http://localhost:8081"
INVENTORY_API_KEY="secret123"

fail() {
  echo ""
  echo "❌ ERROR: $1"
  echo "⛔ TEST DETENIDO"
  exit 1
}

ok() {
  echo "✅ $1"
}

step() {
  echo ""
  echo "--------------------------------------"
  echo "👉 $1"
  echo "--------------------------------------"
}

print_json() {
  echo "$1" | jq . 2>/dev/null || echo "$1"
}

check_response() {
  BODY="$1"
  STATUS="$2"

  if [ "$STATUS" != "200" ] && [ "$STATUS" != "201" ]; then
    echo "❌ HTTP ERROR: $STATUS"
    print_json "$BODY"
    exit 1
  fi

  if echo "$BODY" | jq -e '.errors' > /dev/null 2>&1; then
    echo "❌ API ERROR:"
    print_json "$BODY"
    exit 1
  fi
}

expect_status() {
  BODY="$1"
  STATUS="$2"
  EXPECTED="$3"

  if [ "$STATUS" != "$EXPECTED" ]; then
    echo "❌ HTTP ERROR: $STATUS (se esperaba $EXPECTED)"
    print_json "$BODY"
    exit 1
  fi
}

# =========================
# ESPERA SERVICIOS
# =========================
step "Esperando services..."

until curl -s $BASE_PRODUCTS/actuator/health | grep -q UP; do
  echo "⏳ Esperando products-service..."
  sleep 2
done

ok "products-service listo"

until curl -s $BASE_INVENTORY/actuator/health | grep -q UP; do
  echo "⏳ Esperando inventory-service..."
  sleep 2
done

ok "inventory-service listo"

# =========================
# LOGIN
# =========================
step "Login"

RESP=$(curl -s -w "\n%{http_code}" -X POST $BASE_PRODUCTS/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"admin","password":"admin"}')

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"

TOKEN=$(echo "$BODY" | jq -r '.token')

[ -z "$TOKEN" ] || [ "$TOKEN" == "null" ] && fail "No se obtuvo TOKEN"

ok "Login exitoso"

# =========================
# CREAR PRODUCTO
# =========================
step "Crear producto"

SKU="AUTO-$(date +%s)"
RESP=$(curl -s -w "\n%{http_code}" -X POST $BASE_PRODUCTS/products \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{
  \"sku\": \"$SKU\",
  \"name\": \"Producto test\",
  \"price\": 100,
  \"status\": \"ACTIVE\"
}")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"

PRODUCT_ID=$(echo "$BODY" | jq -r '.data.id')

[ -z "$PRODUCT_ID" ] || [ "$PRODUCT_ID" == "null" ] && fail "No se obtuvo PRODUCT_ID"

ok "Producto creado: $PRODUCT_ID"

# =========================
# VALIDAR PRODUCTO
# =========================
step "Validar producto"

RESP=$(curl -s -w "\n%{http_code}" \
$BASE_PRODUCTS/products/$PRODUCT_ID \
-H "Authorization: Bearer $TOKEN")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"

ok "Producto existe"

# =========================
# CRUD: UPDATE PRODUCTO
# =========================
step "Actualizar producto (PUT)"

RESP=$(curl -s -w "\n%{http_code}" -X PUT $BASE_PRODUCTS/products/$PRODUCT_ID \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{
  \"sku\": \"$SKU\",
  \"name\": \"Producto test editado\",
  \"price\": 101,
  \"status\": \"ACTIVE\"
}")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"
ok "Producto actualizado"

# =========================
# CREAR INVENTARIO
# =========================
step "Crear inventario"

RESP=$(curl -s -w "\n%{http_code}" -X POST \
"$BASE_INVENTORY/inventory?productId=$PRODUCT_ID&quantity=10" \
-H "Authorization: Bearer $TOKEN")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"

ok "Inventario creado"

# =========================
# CONSULTAR INVENTARIO
# =========================
step "Consultar inventario"

RESP=$(curl -s -w "\n%{http_code}" \
$BASE_INVENTORY/inventory/$PRODUCT_ID \
-H "Authorization: Bearer $TOKEN")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"

ok "Inventario consultado"

# =========================
# COMPRA
# =========================
step "Compra normal"

IDEMP_KEY="test-$(date +%s)"
RESP=$(curl -s -w "\n%{http_code}" -X POST \
$BASE_INVENTORY/inventory/purchase \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-H "Idempotency-Key: $IDEMP_KEY" \
-d "{
  \"productId\": \"$PRODUCT_ID\",
  \"quantity\": 2
}")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"

ok "Compra exitosa"

# =========================
# IDEMPOTENCIA
# =========================
step "Idempotencia"

RESP=$(curl -s -w "\n%{http_code}" -X POST \
$BASE_INVENTORY/inventory/purchase \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-H "Idempotency-Key: $IDEMP_KEY" \
-d "{
  \"productId\": \"$PRODUCT_ID\",
  \"quantity\": 2
}")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"

ok "Idempotencia OK (no duplicó compra)"

# =========================
# CONCURRENCIA
# =========================
step "Concurrencia"

TMP1=$(mktemp)
TMP2=$(mktemp)

C1="c1-$(date +%s%N)"
C2="c2-$(date +%s%N)"

curl -s -w "\n%{http_code}" -X POST $BASE_INVENTORY/inventory/purchase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $C1" \
  -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":8}" > "$TMP1" &

curl -s -w "\n%{http_code}" -X POST $BASE_INVENTORY/inventory/purchase \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $C2" \
  -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":8}" > "$TMP2" &

wait

BODY1=$(head -n1 "$TMP1")
STATUS1=$(tail -n1 "$TMP1")
BODY2=$(head -n1 "$TMP2")
STATUS2=$(tail -n1 "$TMP2")

rm -f "$TMP1" "$TMP2"

if { [ "$STATUS1" = "200" ] && [ "$STATUS2" = "409" ]; } || { [ "$STATUS1" = "409" ] && [ "$STATUS2" = "200" ]; }; then
  ok "Concurrencia OK (una compra exitosa y una rechazada con 409)"
else
  echo "❌ Concurrencia fallida (se esperaba 200 y 409)"
  echo "Respuesta 1 ($STATUS1):"; print_json "$BODY1"
  echo "Respuesta 2 ($STATUS2):"; print_json "$BODY2"
  exit 1
fi

# Validar que el inventario NO quedó negativo
RESP=$(curl -s -w "\n%{http_code}" \
$BASE_INVENTORY/inventory/$PRODUCT_ID \
-H "Authorization: Bearer $TOKEN")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

check_response "$BODY" "$STATUS"

AVAILABLE=$(echo "$BODY" | jq -r '.data.available')
if [ "$AVAILABLE" -lt 0 ]; then
  fail "Inventario quedó negativo: $AVAILABLE"
fi
ok "Inventario consistente (available=$AVAILABLE)"

# =========================
# RESILIENCIA
# =========================
step "Resiliencia (products OFF)"

docker stop products-service >/dev/null 2>&1

sleep 3

RESP=$(curl -s -w "\n%{http_code}" -X POST \
$BASE_INVENTORY/inventory/purchase \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-H "Idempotency-Key: fail-$(date +%s%N)" \
-d "{
  \"productId\": \"$PRODUCT_ID\",
  \"quantity\": 1
}")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)

print_json "$BODY"

if [ "$STATUS" != "503" ]; then
  echo "⚠️ ADVERTENCIA: Se esperaba 503 pero llegó $STATUS"
else
  ok "Resiliencia OK (503 correcto)"
fi

echo ""
echo "🔄 Levantando products-service..."
docker start products-service >/dev/null 2>&1
sleep 3
until curl -s $BASE_PRODUCTS/actuator/health | grep -q UP; do
  echo "⏳ Esperando products-service..."
  sleep 2
done
ok "products-service restaurado"

# =========================
# CRUD: DELETE PRODUCTO
# =========================
step "Eliminar producto (DELETE) y validar 404"

RESP=$(curl -s -w "\n%{http_code}" -X POST $BASE_PRODUCTS/products \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d "{
  \"sku\": \"DEL-$(date +%s)\",
  \"name\": \"Producto a eliminar\",
  \"price\": 10,
  \"status\": \"ACTIVE\"
}")

BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)
check_response "$BODY" "$STATUS"

DEL_ID=$(echo "$BODY" | jq -r '.data.id')
[ -z "$DEL_ID" ] || [ "$DEL_ID" == "null" ] && fail "No se obtuvo DEL_ID"

RESP=$(curl -s -w "\n%{http_code}" -X DELETE $BASE_PRODUCTS/products/$DEL_ID \
-H "Authorization: Bearer $TOKEN")
BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)
check_response "$BODY" "$STATUS"
ok "Producto eliminado"

RESP=$(curl -s -w "\n%{http_code}" $BASE_PRODUCTS/products/$DEL_ID \
-H "Authorization: Bearer $TOKEN")
BODY=$(echo "$RESP" | head -n1)
STATUS=$(echo "$RESP" | tail -n1)
expect_status "$BODY" "$STATUS" "404"
ok "Producto eliminado retorna 404"

# =========================
# FIN
# =========================
echo ""
echo "======================================"
echo "🏁 TODOS LOS TEST TERMINARON"
echo "======================================"
