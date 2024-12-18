TranslateResMerger：合并多语言翻译xml文件的工具.

实现原理：
    首先比对已翻译的英语values/strings.xml和本地res/values/strings.xml两个文件，
如果本地values/strings.xml中存在已翻译values/strings.xml的字符key，则把已翻译各
语言下strings.xml中的该key对应的字符文案覆盖到本地各语言values/strings.xml下，如果
本地没有对应语言文件夹，会先创建文件夹。

使用方法
    1.安装插件，重启Android Studio
    2.鼠标定位"res"资源目录，右键弹出列表中，点击底部菜单TranslateResMerger
    3.在弹出窗口中，选择或手动输入已翻译各语言values文件夹上级目录路径，点击"开始合并"，等待完成

版本支持：支持Android Studio 2022.2.1及以后版本
