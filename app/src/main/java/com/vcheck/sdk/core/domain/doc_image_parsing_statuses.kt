package com.vcheck.sdk.core.domain

enum class DocImageParsingStatus {
    SUCCESS,
    IMAGE_NOT_FOUND,
    NORMALIZATION_ERROR,
    DOCUMENT_NOT_FOUND,
    INVALID_DOCUMENT_TYPE,
    INVALID_PARSER,
    INVALID_DOCUMENT_ID,
    INVALID_DOCUMENT_STATUS,
    PAGES_NOT_MATCH,
    ANOMALY_DETECTED,
    UNEXPECTED_ERROR
}

fun DocImageParsingStatus.toCategoryIdx(): Int {
    return when(this) {
        DocImageParsingStatus.SUCCESS -> 0 //успешная загрузка либо парсинг
        DocImageParsingStatus.IMAGE_NOT_FOUND -> 1 //внутренняя ошибка, фронт может её считать как неожиданную и просить пользователя загрузить документ еще раз
        DocImageParsingStatus.NORMALIZATION_ERROR -> 2 //при парсинге мы не смогли выровнять фотографию, фронт может её считать как неверный тип документа
        DocImageParsingStatus.DOCUMENT_NOT_FOUND -> 3 //при парсинге мы не нашли документ на фотографии, фронт может её считать как неверный тип документа
        DocImageParsingStatus.INVALID_DOCUMENT_TYPE -> 4 //при парсинге мы опеределили неверный тип документа
        DocImageParsingStatus.INVALID_PARSER -> 5 //внутренняя ошибка, фронт может её считать как неожиданную и просить пользователя загрузить документ еще раз
        DocImageParsingStatus.INVALID_DOCUMENT_ID -> 6 //внутренняя ошибка, фронт может её считать как неожиданную и просить пользователя загрузить документ еще раз
        DocImageParsingStatus.INVALID_DOCUMENT_STATUS -> 7 //внутренняя ошибка, фронт может её считать как неожиданную и просить пользователя загрузить документ еще раз
        DocImageParsingStatus.PAGES_NOT_MATCH -> 8 //при парсинге мы опеределили, что страницы не принадлежат к одному документу
        DocImageParsingStatus.ANOMALY_DETECTED -> 9 //при детекте на подгруженном фото документа бликов, галограм, сторонних предметов на фото и данных
        else -> 100 //что-то пошло не так при парсинге, просим пользователя загрузить еще раз
    }
}

fun statusCodeToParsingStatus(codeIdx: Int): DocImageParsingStatus {
    return when(codeIdx) {
        0 -> DocImageParsingStatus.SUCCESS //успешная загрузка либо парсинг
        1 -> DocImageParsingStatus.IMAGE_NOT_FOUND //внутренняя ошибка, фронт может её считать как неожиданную и просить пользователя загрузить документ еще раз
        2 -> DocImageParsingStatus.NORMALIZATION_ERROR //при парсинге мы не смогли выровнять фотографию, фронт может её считать как неверный тип документа
        3 -> DocImageParsingStatus.DOCUMENT_NOT_FOUND //при парсинге мы не нашли документ на фотографии, фронт может её считать как неверный тип документа
        4 -> DocImageParsingStatus.INVALID_DOCUMENT_TYPE //при парсинге мы опеределили неверный тип документа
        5 -> DocImageParsingStatus.INVALID_PARSER //внутренняя ошибка, фронт может её считать как неожиданную и просить пользователя загрузить документ еще раз
        6 -> DocImageParsingStatus.INVALID_DOCUMENT_ID //внутренняя ошибка, фронт может её считать как неожиданную и просить пользователя загрузить документ еще раз
        7 -> DocImageParsingStatus.INVALID_DOCUMENT_STATUS //внутренняя ошибка, фронт может её считать как неожиданную и просить пользователя загрузить документ еще раз
        8 -> DocImageParsingStatus.PAGES_NOT_MATCH //при парсинге мы опеределили, что страницы не принадлежат к одному документу
        9 -> DocImageParsingStatus.ANOMALY_DETECTED //при детекте на подгруженном фото документа бликов, галограм, сторонних предметов на фото и данных
        else -> DocImageParsingStatus.UNEXPECTED_ERROR //что-то пошло не так при парсинге, просим пользователя загрузить еще раз
    }
}