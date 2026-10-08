
package com.nexo.marcenaria365.ui.screens.clientes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClienteDetalhesScreen(
    cliente: ClienteUi,
    onVoltar: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: (Long) -> Unit
) {
    var confirmarExclusao by remember {
        mutableStateOf(false)
    }

    BackHandler(onBack = onVoltar)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClienteCores.Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        ClienteCabecalho(
            titulo = "Detalhes do cliente",
            subtitulo = "Informações de cadastro e contato.",
            onVoltar = onVoltar
        )

        Spacer(Modifier.height(26.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClienteAvatar(
                nome = cliente.nome,
                tamanho = 76.dp
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = cliente.nome,
                color = ClienteCores.Azul,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(9.dp))

            ClienteStatus(ativo = cliente.ativo)
        }

        Spacer(Modifier.height(28.dp))

        ClienteTituloSecao("Informações de contato")

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                ClienteInformacao(
                    titulo = "Nome completo",
                    valor = cliente.nome
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 17.dp),
                    color = ClienteCores.Borda
                )

                ClienteInformacao(
                    titulo = "Telefone",
                    valor = cliente.telefone
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 17.dp),
                    color = ClienteCores.Borda
                )

                ClienteInformacao(
                    titulo = "E-mail",
                    valor = cliente.email
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        Button(
            onClick = onEditar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClienteCores.Azul
            )
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(9.dp))

            Text(
                text = "Editar cliente",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                confirmarExclusao = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            border = BorderStroke(
                1.dp,
                Color(0xFFE5C9C7)
            ),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ClienteCores.Vermelho
            )
        ) {
            Icon(
                Icons.Outlined.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(9.dp))

            Text("Excluir cliente")
        }

        Spacer(Modifier.height(28.dp))
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = {
                confirmarExclusao = false
            },
            title = {
                Text(
                    text = "Excluir cliente?",
                    color = ClienteCores.Azul,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Deseja realmente excluir ${cliente.nome}? " +
                            "Essa ação não poderá ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                        onExcluir(cliente.id)
                    }
                ) {
                    Text(
                        "Excluir",
                        color = ClienteCores.Vermelho,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmarExclusao = false
                    }
                ) {
                    Text(
                        "Cancelar",
                        color = ClienteCores.Azul
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
