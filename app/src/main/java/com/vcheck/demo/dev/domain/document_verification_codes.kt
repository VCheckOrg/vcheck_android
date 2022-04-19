package com.vcheck.demo.dev.domain

enum class DocumentVerificationCode {
    OK, //0
    InvalidPagesCount,  //1 - Неверное кол-во загруженных файлов
    InvalidVerificationStage,  //2 - Этап заявки не соответствует разрешенному для данного запроса
    UploadAttemptsExceeded,  //3 - Кол-во попыток на загрузку истекло
    InvalidFiles, //4 - Неверное расширение документа
    InvalidDocumentType, //5 - Неверный тип документа
    PrimaryAlreadyExists,  //6 - Основой документ уже существует
    PhotoTooLarge, //7 - Вес файлов превышает допустимый
}

fun DocumentVerificationCode.toCodeIdx(): Int {
    return when(this) {
        DocumentVerificationCode.OK -> 0
        DocumentVerificationCode.InvalidPagesCount -> 1
        DocumentVerificationCode.InvalidVerificationStage -> 2
        DocumentVerificationCode.UploadAttemptsExceeded -> 3
        DocumentVerificationCode.InvalidFiles -> 4
        DocumentVerificationCode.InvalidDocumentType -> 5
        DocumentVerificationCode.PrimaryAlreadyExists -> 6
        DocumentVerificationCode.PhotoTooLarge -> 7
    }
}

fun codeIdxToVerificationCode(codeIdx: Int)
    : DocumentVerificationCode {
    return when(codeIdx) {
        0 -> DocumentVerificationCode.OK
        1 -> DocumentVerificationCode.InvalidPagesCount
        2 -> DocumentVerificationCode.InvalidVerificationStage
        3 -> DocumentVerificationCode.UploadAttemptsExceeded
        4 -> DocumentVerificationCode.InvalidFiles
        5 -> DocumentVerificationCode.InvalidDocumentType
        6 -> DocumentVerificationCode.PrimaryAlreadyExists
        else -> DocumentVerificationCode.PhotoTooLarge
    }
}