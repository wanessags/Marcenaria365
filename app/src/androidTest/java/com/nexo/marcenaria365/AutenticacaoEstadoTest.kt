package com.nexo.marcenaria365

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import com.nexo.marcenaria365.ui.screens.LoginScreen
import com.nexo.marcenaria365.ui.screens.RecuperacaoSenhaScreen
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AutenticacaoEstadoTest {
    @get:Rule val compose = createComposeRule()

    @Test fun retornoDaRecuperacaoPreservaFormularioDeLogin() {
        compose.setContent { LoginScreen({}, {}, {}) }
        compose.onNodeWithText("E-mail").performTextInput("pessoa@example.test")
        compose.onNodeWithContentDescription("Mostrar senha").performClick()
        compose.onNodeWithText("Senha").performTextInput("SenhaPreservada123!")
        compose.onNodeWithText("Esqueceu sua senha?").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Voltar ao login").performClick()
        compose.onNodeWithText("pessoa@example.test").assertExists()
        compose.onNodeWithText("SenhaPreservada123!").assertExists()
    }

    @Test fun sucessoSobreviveRestauracaoSemRepetirRequisicoes() {
        val solicitacoes = AtomicInteger()
        val redefinicoes = AtomicInteger()
        val sucesso = "Senha atualizada. Entre com sua nova senha."
        val restauracao = StateRestorationTester(compose)
        restauracao.setContent {
            RecuperacaoSenhaScreen(onVoltar = {}, solicitar = {
                solicitacoes.incrementAndGet()
                Result.success("Solicitação recebida.")
            }, redefinir = { _, _ ->
                redefinicoes.incrementAndGet()
                Result.success(sucesso)
            })
        }
        compose.onNodeWithText("E-mail").performTextInput("pessoa@example.test")
        compose.onNodeWithText("Solicitar código").performScrollTo().performClick()
        compose.waitUntil { solicitacoes.get() == 1 }
        compose.waitForIdle()
        compose.onNodeWithText("Se o código chegou por e-mail, informe-o e defina sua nova senha.").assertExists()
        compose.onNodeWithText("Código de recuperação").performScrollTo().performTextInput("a".repeat(64))
        compose.onNodeWithText("Nova senha").performScrollTo().performTextInput("NovaSegura123!")
        compose.onNodeWithText("Confirmar nova senha").performScrollTo().performTextInput("NovaSegura123!")
        compose.onNodeWithText("Atualizar senha").performScrollTo().performClick()
        compose.waitUntil { compose.onAllNodesWithText(sucesso).fetchSemanticsNodes().isNotEmpty() }
        restauracao.emulateSavedInstanceStateRestore()
        compose.onNodeWithText(sucesso).assertExists()
        compose.onNodeWithText("Voltar ao login").assertExists()
        assertEquals(1, solicitacoes.get())
        assertEquals(1, redefinicoes.get())
    }
}
