package com.nothing.plugin

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.nothing.plugin.handle.TranslateResHandler
import java.awt.*
import javax.swing.*
import kotlin.system.exitProcess

class TranslateResMergerAction : AnAction() {

    private var updateComplete = false
    private val resFolderName = "res"

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        var isResFolder = false
        val element: PsiElement? = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (element is PsiDirectory) {
            val directory: PsiDirectory = element
            isResFolder = "res" == directory.name
        }
        e.presentation.isEnabledAndVisible = isResFolder
    }

    override fun actionPerformed(e: AnActionEvent) {
/*        run {
            val project = e.getData(PlatformDataKeys.PROJECT)
            val txt = Messages.showInputDialog(
                project,
                "What is your name?",
                "Input your name",
                Messages.getQuestionIcon()
            )
            Messages.showMessageDialog(
                project,
                "Hello, $txt!\n I am glad to see you.",
                "Information",
                Messages.getInformationIcon()
            )
        }*/

        updateComplete = false
        val resFolderPath = getResFolderPath(e)
        if (resFolderPath == null) {
            Messages.showMessageDialog("请选择名为 'res' 的文件夹", "错误", Messages.getErrorIcon())
        }

        val pathSelector = JFrame("字符串资源处理工具")
        pathSelector.layout = GridLayout(2, 2)

        val translatePanel = JPanel(FlowLayout(FlowLayout.LEFT, 20, 10))
        translatePanel.isVisible = true
        val translateChoose = JButton("选择翻译res路径").apply {
            foreground = Color.ORANGE
            font = Font(null, Font.BOLD, 15)
        }
        val translatePath = JTextField("翻译好的res路径：")
        translatePath.preferredSize = Dimension(500, 50)
        translatePanel.add(translateChoose)
        translatePanel.add(translatePath)
        pathSelector.contentPane.add(translatePanel)

        translateChoose.addActionListener { translatePath.text = chooseResPath(pathSelector) }

        val startPanel = JPanel(FlowLayout(FlowLayout.LEFT, 20, 5))
        startPanel.isVisible = true
        val startMergeRes = JButton("  开始合并  ").apply {
            foreground = Color.ORANGE
            font = Font(null, Font.BOLD, 15)
        }
        //resultMsg.setPreferredSize(new Dimension(600, 50));
        //resultMsg.setPreferredSize(new Dimension(600, 50));
        startPanel.add(startMergeRes)
        pathSelector.contentPane.add(startPanel)
        startMergeRes.addActionListener {
            if (!updateComplete) {
                //new TranslateResMather().match(translatePath.getText(), destPath.getText());
                val result: String = TranslateResHandler.instance!!
                    .traverseFolderAndAppendBase(resFolderPath, translatePath.text)
                updateComplete = true
                startPanel.add(
                    JTextArea().apply {
                        text = result
                    })
                startMergeRes.text = "处理完成"
            } else {
                pathSelector.dispose()
            }
        }

        pathSelector.setSize(800, 200)
        pathSelector.isVisible = true
        pathSelector.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        pathSelector.setLocationRelativeTo(null)
    }

    private fun getResFolderPath(e: AnActionEvent): String? {
        val directory = if (e.getData(CommonDataKeys.PSI_ELEMENT) is PsiDirectory) {
                e.getData(CommonDataKeys.PSI_ELEMENT) as? PsiDirectory
            } else {
                return null
            }
        directory?: return null

        return if (resFolderName == directory.name) {
            directory.virtualFile.canonicalPath
        } else {
            null
        }
    }

    private fun chooseResPath(parent: Component): String? {
        val chooser = JFileChooser("./")
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val result: Int = chooser.showOpenDialog(parent)
        return if (JFileChooser.APPROVE_OPTION == result) {
            chooser.selectedFile.absolutePath
        } else null
    }

//    private fun translateResHandle() {
////        String translateResPath = "D:\\DevelopFiles\\TranslateResMatch\\translate\\res";
////        String destResPath = "D:\\DevelopFiles\\TranslateResMatch\\dest\\res";
//        val translateResPath = "D:\\DevelopFiles\\TranslateResMatch\\0520\\Nothing_OS-strings_0419_settings_23"
//        val destResPath =
//            "D:\\ProgramFiles\\Android\\AndroidStudioProjects\\android13\\NothingExperimental\\common\\src\\main\\res"
//        TranslateResMather().match(translateResPath, destResPath)
//    }
//
//    private fun findAndAppend() {
//        // 工程字符串资源文件路径
//        val baseResPath =
//            "D:\\ProgramFiles\\Android\\AndroidStudioProjects\\android13\\NothingSoundRecorder\\app\\src\\main\\res"
//        // 翻译字符串资源文件路径
//        val finderResPath = "D:\\DevelopFiles\\SoundRecorder\\recorder_original_values\\res"
//        TranslateResHandler.getInstance().traverseFolderAndAppendBase(baseResPath, finderResPath)
//    }
}