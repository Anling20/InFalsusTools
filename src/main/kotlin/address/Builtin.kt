package cn.anling.inftool.address

object BuiltinAddressContainer {
    val default = AddressContainer(
        gameScene = AddressContainer.GameScene(
            classRVA = 0x31A9158,
            staticFieldsOffset = 0xB8,
            instanceReferenceOffset = 0x00,
        ),
        resultsScene = AddressContainer.ResultsScene(
            classRVA = 0x317B710,
        ),
        resultsSceneManager = AddressContainer.ResultsSceneManager(
            classRVA = 0x31BC998,
            staticFieldsOffset = 0xB8,
            instanceReferenceOffset = 0x10,
            array1Offset = 0x30,
            array2Offset = 0x38,
            arrayCountOffset = 0x18,
            arrayDataOffset = 0x20,
            arrayNodeInstanceOffset = 0x18,
        ),
    )
}