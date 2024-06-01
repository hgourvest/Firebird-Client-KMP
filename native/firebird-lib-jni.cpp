#include <jni.h>
#include <ibase.h>
#include <cstring>
#include <algorithm>
#include <limits>
#include <string>

#ifdef _WIN32
    #include <windows.h>
#else
    #include <dlfcn.h>
#endif

static ISC_LONG ISC_EXPORT (*interpret)(ISC_SCHAR*, unsigned int, const ISC_STATUS**);

static ISC_STATUS ISC_EXPORT (*attach_database)(ISC_STATUS *, short, const void *, isc_db_handle *, short, const void *);

static ISC_STATUS ISC_EXPORT (*create_database)(ISC_STATUS*, unsigned short, const void*, isc_db_handle*, unsigned short, const void*, unsigned short);

static ISC_STATUS ISC_EXPORT (*detach_database)(ISC_STATUS *, isc_db_handle *);

static ISC_STATUS ISC_EXPORT (*dsql_execute_immediate)(ISC_STATUS*, isc_db_handle*, isc_tr_handle*, unsigned short, const void*, unsigned short, const XSQLDA*);

static ISC_STATUS ISC_EXPORT_VARARG (*start_transaction)(ISC_STATUS*, isc_tr_handle*, short, ...);

static ISC_STATUS ISC_EXPORT (*commit_retaining)(ISC_STATUS *, isc_tr_handle *);

static ISC_STATUS ISC_EXPORT (*commit_transaction)(ISC_STATUS *, isc_tr_handle *);

static ISC_STATUS ISC_EXPORT (*rollback_retaining)(ISC_STATUS*, isc_tr_handle*);

static ISC_STATUS ISC_EXPORT (*rollback_transaction)(ISC_STATUS*, isc_tr_handle*);

static ISC_STATUS ISC_EXPORT (*dsql_allocate_statement)(ISC_STATUS *, isc_db_handle *, isc_stmt_handle *);

static ISC_STATUS ISC_EXPORT (*dsql_prepare)(ISC_STATUS*, isc_tr_handle*, isc_stmt_handle*, unsigned short, const void*, unsigned short, XSQLDA*);

static ISC_STATUS ISC_EXPORT (*dsql_set_cursor_name)(ISC_STATUS*, isc_stmt_handle*, const ISC_SCHAR*, unsigned short);

static ISC_STATUS ISC_EXPORT (*dsql_describe)(ISC_STATUS *, isc_stmt_handle *, unsigned short, XSQLDA *);

static ISC_STATUS ISC_EXPORT (*dsql_describe_bind)(ISC_STATUS *, isc_stmt_handle *, unsigned short, XSQLDA *);

static ISC_STATUS ISC_EXPORT (*dsql_execute)(ISC_STATUS*, isc_tr_handle*, isc_stmt_handle*, unsigned short, const XSQLDA*);

static ISC_STATUS ISC_EXPORT (*dsql_execute2)(ISC_STATUS*, isc_tr_handle*, isc_stmt_handle*, unsigned short, const XSQLDA*, const XSQLDA*);

static ISC_STATUS ISC_EXPORT (*dsql_fetch)(ISC_STATUS *, isc_stmt_handle *, unsigned short, const XSQLDA *);

static ISC_STATUS ISC_EXPORT (*dsql_free_statement)(ISC_STATUS *, isc_stmt_handle *, unsigned short);

static ISC_STATUS ISC_EXPORT (*open_blob)(ISC_STATUS*, isc_db_handle*, isc_tr_handle*, isc_blob_handle*, ISC_QUAD*);

static ISC_STATUS ISC_EXPORT (*get_segment)(ISC_STATUS *, isc_blob_handle *, unsigned short *, unsigned short, void *);

static ISC_STATUS ISC_EXPORT (*put_segment)(ISC_STATUS*, isc_blob_handle*, unsigned short, const void*);

static ISC_STATUS ISC_EXPORT (*blob_info)(ISC_STATUS*, isc_blob_handle*, short, const void*, short, void*);

static ISC_STATUS ISC_EXPORT (*close_blob)(ISC_STATUS *, isc_blob_handle *);

static ISC_STATUS ISC_EXPORT (*create_blob)(ISC_STATUS*, isc_db_handle*, isc_tr_handle*, isc_blob_handle*, ISC_QUAD*);

static ISC_STATUS ISC_EXPORT (*dsql_sql_info)(ISC_STATUS*, isc_stmt_handle*, short, const ISC_SCHAR*, short, ISC_SCHAR*);



void throwDataConversionError(JNIEnv* env, int column) {
    jclass exceptionClass = env->FindClass("com/progdigy/fbclient/FirebirdException");

    if (exceptionClass != nullptr) {
        std::string str = "Data type conversion error (" + std::to_string(column) + ")";
        env->ThrowNew(exceptionClass, str.c_str());
    }
}

void throwNullError(JNIEnv* env) {
    jclass exceptionClass = env->FindClass("com/progdigy/fbclient/FirebirdException");

    if (exceptionClass != nullptr) {
        env->ThrowNew(exceptionClass, "Field is null");
    }
}

void throwOutOfBoundError(JNIEnv* env, int index) {
    jclass exceptionClass = env->FindClass("com/progdigy/fbclient/FirebirdException");

    if (exceptionClass != nullptr) {
        std::string str = "Index out of bound: " + std::to_string(index);
        env->ThrowNew(exceptionClass, str.c_str());
    }
}

void throwHandleError(JNIEnv* env) {
    jclass exceptionClass = env->FindClass("com/progdigy/fbclient/FirebirdException");

    if (exceptionClass != nullptr) {
        env->ThrowNew(exceptionClass, "Invalid Handle value");
    }
}

void throwStringTruncation(JNIEnv* env, int index) {
    jclass exceptionClass = env->FindClass("com/progdigy/fbclient/FirebirdException");

    if (exceptionClass != nullptr) {
        std::string str = "String truncation: " + std::to_string(index);
        env->ThrowNew(exceptionClass, str.c_str());
    }
}

void throwLoadLibraryError(JavaVM* vm) {
    JNIEnv* env;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_OK) {
        jclass exceptionClass = env->FindClass("com/progdigy/fbclient/FirebirdException");

        if (exceptionClass != nullptr) {
            env->ThrowNew(exceptionClass, "Error loading Firebird client library");
        }
    }
}

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {

#ifdef _WIN32
    HINSTANCE handle = NULL;
    if (!(handle = LoadLibrary("fbclient.dll"))) {
        throwLoadLibraryError(vm);
        return JNI_ERR;
    }

    *(FARPROC *) (&interpret) = GetProcAddress(handle, "fb_interpret");
    *(FARPROC *) (&attach_database) = GetProcAddress(handle, "isc_attach_database");
    *(FARPROC *) (&create_database) = GetProcAddress(handle, "isc_create_database");
    *(FARPROC *) (&detach_database) = GetProcAddress(handle, "isc_detach_database");
    *(FARPROC *) (&dsql_execute_immediate) = GetProcAddress(handle, "isc_dsql_execute_immediate");
    *(FARPROC *) (&start_transaction) = GetProcAddress(handle, "isc_start_transaction");
    *(FARPROC *) (&commit_retaining) = GetProcAddress(handle, "isc_commit_retaining");
    *(FARPROC *) (&commit_transaction) = GetProcAddress(handle, "isc_commit_transaction");
    *(FARPROC *) (&rollback_retaining) = GetProcAddress(handle, "isc_rollback_retaining");
    *(FARPROC *) (&rollback_transaction) = GetProcAddress(handle, "isc_rollback_transaction");
    *(FARPROC *) (&dsql_allocate_statement) = GetProcAddress(handle, "isc_dsql_allocate_statement");
    *(FARPROC *) (&dsql_prepare) = GetProcAddress(handle, "isc_dsql_prepare");
    *(FARPROC *) (&dsql_set_cursor_name) = GetProcAddress(handle, "isc_dsql_set_cursor_name");
    *(FARPROC *) (&dsql_describe) = GetProcAddress(handle, "isc_dsql_describe");
    *(FARPROC *) (&dsql_describe_bind) = GetProcAddress(handle, "isc_dsql_describe_bind");
    *(FARPROC *) (&dsql_execute) = GetProcAddress(handle, "isc_dsql_execute");
    *(FARPROC *) (&dsql_execute2) = GetProcAddress(handle, "isc_dsql_execute2");
    *(FARPROC *) (&dsql_fetch) = GetProcAddress(handle, "isc_dsql_fetch");
    *(FARPROC *) (&dsql_free_statement) = GetProcAddress(handle, "isc_dsql_free_statement");
    *(FARPROC *) (&open_blob) = GetProcAddress(handle, "isc_open_blob");
    *(FARPROC *) (&get_segment) = GetProcAddress(handle, "isc_get_segment");
    *(FARPROC *) (&put_segment) = GetProcAddress(handle, "isc_put_segment");
    *(FARPROC *) (&blob_info) = GetProcAddress(handle, "isc_blob_info");
    *(FARPROC *) (&close_blob) = GetProcAddress(handle, "isc_close_blob");
    *(FARPROC *) (&create_blob) = GetProcAddress(handle, "isc_create_blob");
    *(FARPROC *) (&dsql_sql_info) = GetProcAddress(handle, "isc_dsql_sql_info");

#else
    #ifdef __APPLE__
        #define LIB_FBCLIENT "libfbclient.dylib"
        #define LIB_FBCLIENT2 "/Library/Frameworks/Firebird.framework/Resources/lib/libfbclient.dylib"
    #else
        #define LIB_FBCLIENT "libfbclient.so"
        #define LIB_FBCLIENT2 "/opt/firebird/lib/libfbclient.so"
    #endif

    void* handle = nullptr;
    if (!(handle = dlopen(LIB_FBCLIENT, RTLD_NOW))) {
        if (!(handle = dlopen(LIB_FBCLIENT2, RTLD_NOW))) {
            throwLoadLibraryError(vm);
            return JNI_ERR;
        }
    }

    *(void **) (&interpret) = dlsym(handle, "fb_interpret");
    *(void **) (&attach_database) = dlsym(handle, "isc_attach_database");
    *(void **) (&create_database) = dlsym(handle, "isc_create_database");
    *(void **) (&detach_database) = dlsym(handle, "isc_detach_database");
    *(void **) (&dsql_execute_immediate) = dlsym(handle, "isc_dsql_execute_immediate");
    *(void **) (&start_transaction) = dlsym(handle, "isc_start_transaction");
    *(void **) (&commit_retaining) = dlsym(handle, "isc_commit_retaining");
    *(void **) (&commit_transaction) = dlsym(handle, "isc_commit_transaction");
    *(void **) (&rollback_retaining) = dlsym(handle, "isc_rollback_retaining");
    *(void **) (&rollback_transaction) = dlsym(handle, "isc_rollback_transaction");
    *(void **) (&dsql_allocate_statement) = dlsym(handle, "isc_dsql_allocate_statement");
    *(void **) (&dsql_prepare) = dlsym(handle, "isc_dsql_prepare");
    *(void **) (&dsql_set_cursor_name) = dlsym(handle, "isc_dsql_set_cursor_name");
    *(void **) (&dsql_describe) = dlsym(handle, "isc_dsql_describe");
    *(void **) (&dsql_describe_bind) = dlsym(handle, "isc_dsql_describe_bind");
    *(void **) (&dsql_execute) = dlsym(handle, "isc_dsql_execute");
    *(void **) (&dsql_execute2) = dlsym(handle, "isc_dsql_execute2");
    *(void **) (&dsql_fetch) = dlsym(handle, "isc_dsql_fetch");
    *(void **) (&dsql_free_statement) = dlsym(handle, "isc_dsql_free_statement");
    *(void **) (&open_blob) = dlsym(handle, "isc_open_blob");
    *(void **) (&get_segment) = dlsym(handle, "isc_get_segment");
    *(void **) (&put_segment) = dlsym(handle, "isc_put_segment");
    *(void **) (&blob_info) = dlsym(handle, "isc_blob_info");
    *(void **) (&close_blob) = dlsym(handle, "isc_close_blob");
    *(void **) (&create_blob) = dlsym(handle, "isc_create_blob");
    *(void **) (&dsql_sql_info) = dlsym(handle, "isc_dsql_sql_info");
#endif

    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM* vm, void* reserved) {
    // Libération des ressources ici
}

constexpr ISC_STATUS ISC_MASK	= FB_IMPL_MSG_MASK;	// Defines the code as a valid ISC code
constexpr ISC_STATUS FAC_MASK	= 0x00FF0000;	// Specifies the facility where the code is located
constexpr ISC_STATUS CODE_MASK	= 0x0000FFFF;	// Specifies the code in the message file
constexpr ISC_STATUS CLASS_MASK	= 0xF0000000;	// Defines the code as warning, error, info, or other

#define CLASS_ERROR		0L		// Code represents an error
#define CLASS_WARNING		1L		// Code represents a warning
#define CLASS_INFO		2L		// Code represents an information msg

jlong checkStatus(JNIEnv* env, const ISC_STATUS* statusArray, jlong code) {
    if (code != 0) {
        if (((code & CLASS_MASK) >> 30) == CLASS_ERROR) {
            jclass exceptionClass = env->FindClass("com/progdigy/fbclient/FirebirdException");
            if (exceptionClass != nullptr) {
                ISC_SCHAR buffer[1024] = {0};
                auto len = interpret(buffer, sizeof(buffer), &statusArray);
                auto total = len;
                while (len > 0 && total < sizeof buffer) {
                    buffer[total++] = '\n';
                    len = interpret(buffer + total, sizeof(buffer) - total, &statusArray);
                    total += len;
                }
                env->ThrowNew(exceptionClass, buffer);
            }
        }
    }
    return code;
}

jlong checkStatus(JNIEnv* env, jlong status, jlong code) {
    const ISC_STATUS* statusArray = (ISC_STATUS*)(status);
    return checkStatus(env, statusArray, code);
}

template<typename T, T (*block)(JNIEnv*, ISC_STATUS*, FB_API_HANDLE*, FB_API_HANDLE*, int, ISC_SCHAR*, ISC_SHORT, ISC_SHORT, ISC_SHORT)>
inline T getFieldValue(JNIEnv *env, jlong status, jlong db_handle, jlong tr_handle, jlong sqlda, int index) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE*>(db_handle);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    auto p = (handle != nullptr)?*handle: nullptr;
    if (p != nullptr) {
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            if (v->sqlind == nullptr || *v->sqlind == 0) {
                return block(env, statusArray, dbHandle, trHandle, index, v->sqldata, v->sqltype & ~1, v->sqllen, v->sqlsubtype);
            } else
                throwNullError(env);
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
    return 0;
}

template<typename T, T (*block)(JNIEnv*, int, ISC_SCHAR*, ISC_SHORT, ISC_SHORT, ISC_SHORT)>
inline T getFieldValue(JNIEnv *env, jlong sqlda, int index) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    auto p = (handle != nullptr)?*handle: nullptr;
    if (p != nullptr) {
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            if (v->sqlind == nullptr || *v->sqlind == 0) {
                return block(env, index, v->sqldata, v->sqltype & ~1, v->sqllen, v->sqlsubtype);
            } else
                throwNullError(env);
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
    return 0;
}

template<typename T, T (*block)(JNIEnv*, int, ISC_SCHAR*, ISC_SHORT)>
inline T getFieldValue(JNIEnv *env, jlong sqlda, int index) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    auto p = (handle != nullptr)?*handle: nullptr;
    if (p != nullptr) {
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            if (v->sqlind == nullptr || *v->sqlind == 0) {
                return block(env, index, v->sqldata, v->sqltype & ~1);
            } else
                throwNullError(env);
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
    return 0;
}


template<typename T, void (*block)(JNIEnv*, ISC_STATUS*, FB_API_HANDLE*, FB_API_HANDLE*, int, ISC_SCHAR*, ISC_SHORT, ISC_SHORT, ISC_SHORT, T)>
inline void setFieldValue(JNIEnv *env, jlong status, jlong db_handle, jlong tr_handle, jlong sqlda, int index, T value) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE*>(db_handle);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    auto p = (handle != nullptr)?*handle: nullptr;
    if (p != nullptr) {
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            block(env, statusArray, dbHandle, trHandle, index, v->sqldata, v->sqltype & ~1, v->sqllen, v->sqlsubtype, value);
            if (v->sqlind != nullptr)
                *v->sqlind = 0;
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
}

template<typename T, void (*block)(JNIEnv*, int, ISC_SCHAR*, ISC_SHORT, ISC_SHORT, ISC_SHORT, T)>
inline void setFieldValue(JNIEnv *env, jlong sqlda, int index, T value) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    auto p = (handle != nullptr)?*handle: nullptr;
    if (p != nullptr) {
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            block(env, index, v->sqldata, v->sqltype & ~1, v->sqllen, v->sqlsubtype, value);
            if (v->sqlind != nullptr)
                *v->sqlind = 0;
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
}

template<typename T, void (*block)(JNIEnv*, int, ISC_SCHAR*, ISC_SHORT, T)>
inline void setFieldValue(JNIEnv *env, jlong sqlda, int index, T value) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    auto p = (handle != nullptr)?*handle: nullptr;
    if (p != nullptr) {
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            block(env, index, v->sqldata, v->sqltype & ~1, value);
            if (v->sqlind != nullptr)
                *v->sqlind = 0;
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
}


/**
 * @brief Allocates memory for the data buffer of an XSQLDA structure.
 *
 * This function allocates memory for a data buffer based on the information provided in the specified XSQLDA structure.
 * The data buffer is used to store the actual data values for each field in the XSQLDA structure.
 *
 * @param sqlda The XSQLDA structure containing the field definitions.
 *
 * @note The XSQLDA structure should be pre-initialized with the correct values for version, sqldaid, sqldabc, sqln, and sqld.
 *       The sqlvar array should contain the field definitions.
 */
void allocateDataBuffer(XSQLDA *sqlda) {
    size_t total = 0;
    for (int i = 0; i < sqlda->sqld; i ++) {
        auto var = &sqlda->sqlvar[i];
        var->sqldata = (ISC_SCHAR*)total;
        switch (var->sqltype & ~1) {
            case SQL_TEXT:
                // + zero terminal
                total += var->sqllen + 1;
                break;
            case SQL_VARYING:
                // size of PARAMVARY + zero terminal
                total += sizeof (ISC_USHORT) + var->sqllen + 1;
                break;
            case SQL_FLOAT:
            case SQL_D_FLOAT:
            case SQL_DOUBLE:
                // scale is irrelevant and lead to duplicate code
                var->sqlscale = 0;
                total += var->sqllen;
                break;
            default:
                total += var->sqllen;
        }

        if ((var->sqltype & 1) == 1) {
            var->sqlind = (ISC_SHORT *)total;
            total += sizeof (short);
        } else
            var->sqlind = nullptr;
    }
    auto buffer = (ISC_SCHAR *)malloc(total);
    memset(buffer, 0, total);
    for (int i = 0; i < sqlda->sqld; i ++) {
        auto var = &sqlda->sqlvar[i];
        var->sqldata = buffer + (size_t)var->sqldata;
        if (var->sqlind != nullptr) {
            var->sqlind = (ISC_SHORT *)(buffer + (size_t)var->sqlind);
            *var->sqlind = -1; // nullables are null
        }

    }
}

/**
 * @brief Calculates the size of a UTF-8 string.
 *
 * This function calculates the size of a UTF-8 string by iterating
 * through each character in the string and counting the number of
 * valid UTF-8 characters. The maximum length of the string and the
 * maximum number of UTF-8 characters to count can be specified.
 *
 * @param string The UTF-8 string.
 * @param maxlength The maximum length of the string to process.
 * @param maxsize The maximum number of UTF-8 characters to count.
 *
 * @return The size of the UTF-8 string in number of UTF-8 characters.
 */
static size_t utf8_size(char* string, int maxlength, short maxsize) {
    size_t length = 0;
    size_t size = 0;
    while (*string != 0) {
        if ((*string++ & 0xC0) != 0x80) ++length;
        if (length > maxlength || size++ == maxsize)
            break;
    }
    return size;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_allocStatusArray(JNIEnv *env, jclass clazz) {
    auto status = new ISC_STATUS_ARRAY;
    for(int i = 0; i < ISC_STATUS_LENGTH; i++) {
        status[i] = 0;
    }
    return reinterpret_cast<jlong>(status);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_allocHandle(JNIEnv *env, jclass clazz) {
    auto handle = (void**)malloc(sizeof (void*));
    *handle = nullptr;
    return reinterpret_cast<jlong>(handle);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_freeHandle(JNIEnv *env, jclass clazz, jlong handle) {
    if (handle != 0)
        free((void*)(handle));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_freeStatusArray(JNIEnv *env, jclass clazz, jlong status) {
    if (status != 0)
        delete[] reinterpret_cast<ISC_STATUS_ARRAY*>(status);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_attachDatabase(
        JNIEnv *env, jclass clazz, jlong status, jstring path,jlong db_handle, jbyteArray options) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE *>(db_handle);
    const char *dbPath = env->GetStringUTFChars(path, nullptr);
    auto len = (options != nullptr)? env->GetArrayLength(options): 0;
    auto dpb = (len > 0) ? env->GetByteArrayElements(options, nullptr) : nullptr;
    auto ret = attach_database(statusArray, (short)std::strlen(dbPath), dbPath, dbHandle, (short)len, (ISC_SCHAR *)dpb);
    if (dpb != nullptr)
        env->ReleaseByteArrayElements(options, dpb, 0);
    env->ReleaseStringUTFChars(path, dbPath);
    return ret;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_createDatabase(
        JNIEnv *env, jclass clazz, jlong status, jstring path,jlong db_handle, jbyteArray options) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE *>(db_handle);
    const char *dbPath = env->GetStringUTFChars(path, nullptr);
    auto len = (options != nullptr)? env->GetArrayLength(options): 0;
    auto dpb = (len > 0) ? env->GetByteArrayElements(options, nullptr) : nullptr;
    auto ret = create_database(statusArray, (short)std::strlen(dbPath), dbPath, dbHandle, (short)len, (ISC_SCHAR *)dpb, 0);
    if (dpb != nullptr)
        env->ReleaseByteArrayElements(options, dpb, 0);
    env->ReleaseStringUTFChars(path, dbPath);
    return ret;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_detachDatabase(
        JNIEnv *env, jclass clazz, jlong status, jlong db_handle) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE*>(db_handle);
    return detach_database(statusArray, dbHandle);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_executeImmediate(JNIEnv *env, jclass clazz, jlong status,
                                                    jlong db_handle, jlong tr_handle, jstring sql, jshort dialect) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE*>(db_handle);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    auto string = env->GetStringUTFChars(sql, nullptr);
    auto ret = dsql_execute_immediate(statusArray, dbHandle, trHandle, strlen(string), string, dialect, nullptr);
    env->ReleaseStringUTFChars(sql, string);
    return ret;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_progdigy_fbclient_API_interpret(JNIEnv *env, jclass clazz, jlong status) {
    ISC_SCHAR buffer[1024] = {0};
    const ISC_STATUS* statusArray = (ISC_STATUS*)(status);
    auto len = interpret(buffer, sizeof(buffer), &statusArray);
    auto total = len;
    while (len > 0 && total < sizeof buffer) {
        buffer[total++] = '\n';
        len = interpret(buffer + total, sizeof(buffer) - total, &statusArray);
        total += len;
    }
    return env->NewStringUTF(buffer);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_startTransaction(JNIEnv *env, jclass clazz, jlong status,
                                                jlong tr_handle, jlong db_handle, jbyteArray options) {
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE*>(db_handle);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    auto len = (options != nullptr)? env->GetArrayLength(options): 0;
    auto tpb = (len > 0)?env->GetByteArrayElements(options, nullptr): nullptr;
    auto ret = start_transaction(statusArray, trHandle, 1, dbHandle, (short)len, (ISC_SCHAR *)tpb);
    if (tpb != nullptr)
        env->ReleaseByteArrayElements(options, tpb, 0);
    return ret;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_commitTransaction(JNIEnv *env, jclass clazz, jlong status, jlong tr_handle, jboolean retain) {
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    if (retain)
        return commit_retaining(statusArray, trHandle);
    else
        return commit_transaction(statusArray, trHandle);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_rollbackTransaction(JNIEnv *env, jclass clazz, jlong status, jlong tr_handle, jboolean retain) {
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    if (retain)
        return rollback_retaining(statusArray, trHandle);
    else
        return rollback_transaction(statusArray, trHandle);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_prepareStatement(JNIEnv *env, jclass clazz, jlong status, jlong db_handle, jlong tr_handle,
    jlong st_handle, jstring sql, jstring cursor, jshort dialect, jlong sqlda) {

    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE*>(db_handle);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    auto stHandle = reinterpret_cast<FB_API_HANDLE*>(st_handle);
    auto xsqlda   = reinterpret_cast<XSQLDA **>(sqlda);
    const char *statement = env->GetStringUTFChars(sql, nullptr);
    auto ret = dsql_allocate_statement(statusArray, dbHandle, stHandle);
    if (ret != 0) return ret;
    XSQLDA da = {0};
    da.version = SQLDA_VERSION1;
    ret = dsql_prepare(statusArray, trHandle, stHandle, strlen(statement), statement, dialect, &da);
    env->ReleaseStringUTFChars(sql, statement);
    if (ret == 0) {
        if (cursor != nullptr) {
            const char *name = env->GetStringUTFChars(cursor, nullptr);
            ret = dsql_set_cursor_name(statusArray, stHandle, name, 0);
            env->ReleaseStringUTFChars(cursor, name);
        }
        if (ret == 0 && xsqlda != nullptr && da.sqld > 0) {
            auto len = XSQLDA_LENGTH(da.sqld);
            auto pXSQLDA = (XSQLDA*)malloc(len);
            memset(pXSQLDA, 0, len);
            pXSQLDA->version = SQLDA_VERSION1;
            pXSQLDA->sqln = da.sqld;
            ret = dsql_describe(statusArray, stHandle, dialect, pXSQLDA);
            if (ret == 0) {
                *xsqlda = pXSQLDA;
                allocateDataBuffer(pXSQLDA);
            } else {
                free(pXSQLDA);
                return ret;
            }
        }
    }
    return ret;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_getStatementType(JNIEnv *env, jclass clazz, jlong status, jlong st_handle) {
    ISC_SCHAR data[9] = {isc_info_sql_stmt_type};
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto stHandle = reinterpret_cast<FB_API_HANDLE*>(st_handle);
    if (stHandle == nullptr || *stHandle == 0)
        throwHandleError(env);
    dsql_sql_info(statusArray, stHandle, 1, (ISC_SCHAR*)&data, 8, (ISC_SCHAR*)&data[1]);
    return data[4] - 1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_freeSQLDA(JNIEnv *env, jclass clazz, jlong handle) {
    auto h = reinterpret_cast<XSQLDA **>(handle);
    if (h != nullptr) {
        auto sqlda = *h;
        if (sqlda != nullptr) {
            free(sqlda->sqlvar[0].sqldata);
            free(sqlda);
            *h = nullptr;
        }
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_freeStatement(JNIEnv *env, jclass clazz, jlong status, jlong st_handle, jshort action) {
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto stHandle = reinterpret_cast<FB_API_HANDLE*>(st_handle);
    auto ret = dsql_free_statement(statusArray, stHandle, action);
    if (action == DSQL_drop)
        *stHandle = 0;
    return ret;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_prepareParams(JNIEnv *env, jclass clazz, jlong status, jlong statement, jshort dialect, jlong sqlda) {
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto stHandle = reinterpret_cast<FB_API_HANDLE*>(statement);
    auto xsqlda   = reinterpret_cast<XSQLDA **>(sqlda);

    XSQLDA da = {0};
    da.version = SQLDA_VERSION1;
    da.sqld = 0;
    da.sqln = 0;
    auto ret = dsql_describe_bind(statusArray, stHandle, dialect, &da);
    if (ret == 0 && da.sqld > 0) {
        auto len = XSQLDA_LENGTH(da.sqld);
        auto pXSQLDA = (XSQLDA*)malloc(len);
        memset(pXSQLDA, 0, len);
        pXSQLDA->version = SQLDA_VERSION1;
        pXSQLDA->sqln = da.sqld;
        ret = dsql_describe_bind(statusArray, stHandle, dialect, pXSQLDA);
        if (ret == 0) {
            *xsqlda = pXSQLDA;
            allocateDataBuffer(pXSQLDA);
        } else {
            free(pXSQLDA);
        }
    }

    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setIsNull(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    if (handle != nullptr) {
        auto p = *handle;
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            if (v->sqlind != nullptr)
                *v->sqlind = -1;
            else
                throwDataConversionError(env, index);
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_getScale(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    if (handle != nullptr) {
        auto p = *handle;
        if (index >= 0 && index < p->sqld) {
            return p->sqlvar[index].sqlscale;
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_getType(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    if (handle != nullptr) {
        auto p = *handle;
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            switch (v->sqltype & ~1) {
                case SQL_SHORT: return 0;
                case SQL_LONG : return 1;
                case SQL_QUAD :
                case SQL_INT64: return 2;
                case SQL_FLOAT: return 3;
                case SQL_D_FLOAT:
                case SQL_DOUBLE: return 4;
                case SQL_TEXT:
                case SQL_VARYING:
                    if (v->sqlsubtype != 0)
                        return 5;
                    else
                        return 6;
                case SQL_INT128:
                    return 7;
                case SQL_BOOLEAN:
                    return 8;
                case SQL_TYPE_DATE:
                    return 9;
                case SQL_TYPE_TIME:
                    return 10;
                case SQL_TIMESTAMP:
                    return 11;
                case SQL_TIME_TZ:
                case SQL_TIME_TZ_EX:
                    return 12;
                case SQL_TIMESTAMP_TZ:
                case SQL_TIMESTAMP_TZ_EX:
                    return 13;
                case SQL_BLOB:
                    return v->sqlsubtype == 1?15:14;
                default:
                    return -1;
            }
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
    return 0;
}

jstring getName(JNIEnv *env, jlong sqlda, jint index, int kind) {
    char data[33] = {0};
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    if (handle != nullptr) {
        auto p = *handle;
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            switch (kind) {
                case 0:
                    memcpy(data, v->sqlname, v->sqlname_length);
                    data[v->sqlname_length] = 0;
                    break;
                case 1:
                    memcpy(data, v->relname, v->relname_length);
                    data[v->relname_length] = 0;
                    break;
                case 2:
                    memcpy(data, v->ownname, v->ownname_length);
                    data[v->ownname_length] = 0;
                    break;
                case 3:
                    memcpy(data, v->aliasname, v->aliasname_length);
                    data[v->aliasname_length] = 0;
                    break;
                default:
                    // nop
                    break;
            }
            return env->NewStringUTF(data);
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
    return nullptr;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_progdigy_fbclient_API_getName(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getName(env, sqlda, index, 0);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_progdigy_fbclient_API_getRelation(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getName(env, sqlda, index, 1);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_progdigy_fbclient_API_getOwner(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getName(env, sqlda, index, 2);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_progdigy_fbclient_API_getAlias(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getName(env, sqlda, index, 3);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_getCount(JNIEnv *env, jclass clazz, jlong sqlda) {
    char data[33] = {0};
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    if (handle != nullptr) {
        auto p = *handle;
        return p->sqld;
    } else
        throwHandleError(env);
    return 0;
}

inline void setValueBoolean(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jboolean value) {
    switch (code) {
        case SQL_BOOLEAN:
            *(ISC_UCHAR *)data = value?FB_TRUE:FB_FALSE;
            break;
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueBoolean(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jboolean value) {
    setFieldValue<jboolean, setValueBoolean>(env, sqlda, index, value);
}

inline void setValueShort(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jshort value) {
    switch (code) {
        case SQL_SHORT:
            *(ISC_SHORT*)data = value;
            break;
        case SQL_LONG :
            *(ISC_LONG*)data  = value;
            break;
        case SQL_INT64:
        case SQL_QUAD:
            *(ISC_INT64*)data = value;
            break;
        case SQL_FLOAT:
            *(float *)data = value;
            break;
        case SQL_D_FLOAT:
        case SQL_DOUBLE:
            *(double *)data = value;
            break;
        case SQL_INT128: {
            auto v = (FB_I128 *)data;
            v->fb_data[0] = value;
            v->fb_data[1] = value < 0 ? -1 : 0;
            break;
        }
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueShort(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jshort value) {
    setFieldValue<jshort , setValueShort>(env, sqlda, index, value);
}

inline void setValueInt(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jint value) {
    switch (code) {
        case SQL_LONG :
            *(ISC_LONG*)data  = value;
            break;
        case SQL_INT64:
        case SQL_QUAD:
            *(ISC_INT64*)data = value;
            break;
        case SQL_D_FLOAT:
        case SQL_DOUBLE:
            *(double *)data = value;
            break;
        case SQL_INT128: {
            auto v = (FB_I128 *)data;
            v->fb_data[0] = value;
            v->fb_data[1] = value < 0 ? -1 : 0;
            break;
        }
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueInt(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jint value) {
    setFieldValue<jint, setValueInt>(env, sqlda, index, value);
}

inline void setValueLong(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jlong value) {
    switch (code) {
        case SQL_INT64:
        case SQL_QUAD:
            *(ISC_INT64*)data = value;
            break;
        case SQL_INT128: {
            auto v = (FB_I128 *)data;
            v->fb_data[0] = value;
            v->fb_data[1] = value < 0 ? -1 : 0;
            break;
        }
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueLong(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jlong value) {
    setFieldValue<jlong, setValueLong>(env, sqlda, index, value);
}

inline void setValueString(JNIEnv* env, ISC_STATUS* status, FB_API_HANDLE* dbHandle, FB_API_HANDLE* trHandle,
                           int index, ISC_SCHAR* data, ISC_SHORT code, ISC_SHORT len, ISC_SHORT subtype, jstring value) {
    switch (code) {
        case SQL_VARYING:
            if (subtype == 4) {
                auto strLen = env->GetStringLength(value);
                if (strLen <= len / 4) {
                    auto str = env->GetStringUTFChars(value, nullptr);
                    auto size = strlen(str);
                    auto vary = (PARAMVARY *)data;
                    memcpy(vary->vary_string, str, size);
                    vary->vary_length = size;
                    env->ReleaseStringUTFChars(value, str);
                } else
                    throwStringTruncation(env, index);
            } else
                throwDataConversionError(env, index);
            break;
        case SQL_TEXT:
            if (subtype == 4) {
                auto strLen = env->GetStringLength(value);
                if (strLen <= len / 4) {
                    auto str = env->GetStringUTFChars(value, nullptr);
                    auto size = strlen(str);
                    memset(data, ' ',len);
                    memcpy(data, str, size);
                    env->ReleaseStringUTFChars(value, str);
                } else
                    throwStringTruncation(env, index);
            } else
                throwDataConversionError(env, index);
            break;
        case SQL_BLOB:
            if (subtype == 1) {
                isc_blob_handle blob = 0;
                auto ret = create_blob(status, dbHandle, trHandle, &blob, (GDS_QUAD*)data);
                if (ret == 0) {
                    auto bytes = env->GetStringUTFChars(value, nullptr);
                    auto length = strlen(bytes);
                    auto toWrite = std::min(length, (size_t)std::numeric_limits<ISC_SHORT>::max());
                    auto p = bytes;
                    while (put_segment(status, &blob, toWrite, p) == 0) {
                        length -= toWrite;
                        if (length <= 0)
                            break;
                        toWrite = std::min(length, (size_t)std::numeric_limits<ISC_SHORT>::max());
                        p += toWrite;
                    }
                    env->ReleaseStringUTFChars(value, bytes);
                    ret = close_blob(status, &blob);
                }
                checkStatus(env, status, ret);
            } else
                throwDataConversionError(env, index);
            break;
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueString(JNIEnv *env, jclass clazz, jlong status, jlong db_handle, jlong tr_handle,
                                              jlong sqlda, jint index, jstring value) {
    setFieldValue<jstring, setValueString>(env, status, db_handle, tr_handle, sqlda, index, value);
}

inline void setValueByteArray(JNIEnv* env, ISC_STATUS* status, FB_API_HANDLE* dbHandle, FB_API_HANDLE* trHandle,
                              int index, ISC_SCHAR* data,  ISC_SHORT code, ISC_SHORT len, ISC_SHORT subtype, jbyteArray value) {
    switch (code) {
        case SQL_VARYING: {
            auto size = env->GetArrayLength(value);
            if (size <= len) {
                auto array = env->GetByteArrayElements(value, nullptr);
                auto vary = (PARAMVARY *)data;
                memcpy(vary->vary_string, array, size);
                vary->vary_length = size;
                env->ReleaseByteArrayElements(value, array, JNI_COMMIT);
            } else
                throwStringTruncation(env, index);
            break;
        }
        case SQL_TEXT: {
            auto size = env->GetArrayLength(value);
            if (size <= len) {
                auto array = env->GetByteArrayElements(value, nullptr);
                memset(data, subtype > 0?' ':0,len);
                memcpy(data, array, size);
                env->ReleaseByteArrayElements(value, array, JNI_COMMIT);
            } else
                throwStringTruncation(env, index);
            break;
        }
        case SQL_BLOB: {
            isc_blob_handle blob = 0;
            auto ret = create_blob(status, dbHandle, trHandle, &blob, (GDS_QUAD*)data);
            if (ret == 0) {
                auto length = env->GetArrayLength(value);
                auto bytes = env->GetByteArrayElements(value, nullptr);
                auto toWrite = std::min(length, (jsize)std::numeric_limits<ISC_SHORT>::max());
                auto p = bytes;
                while (put_segment(status, &blob, toWrite, p) == 0) {
                    length -= toWrite;
                    if (length <= 0)
                        break;
                    toWrite = std::min(length, (jsize)std::numeric_limits<ISC_SHORT>::max());
                    p += toWrite;
                }
                env->ReleaseByteArrayElements(value, bytes, JNI_COMMIT);
                ret = close_blob(status, &blob);
            }
            checkStatus(env, status, ret);
            break;
        }
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueByteArray(JNIEnv *env, jclass clazz, jlong status, jlong db_handle, jlong tr_handle,
                                                 jlong sqlda, jint index, jbyteArray value) {
    setFieldValue<jbyteArray, setValueByteArray>(env, status, db_handle, tr_handle, sqlda, index, value);
}

inline void setValueFloat(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jfloat value) {
    switch (code) {
        case SQL_FLOAT:
            *(float *)data = value;
            break;
        case SQL_D_FLOAT:
        case SQL_DOUBLE:
            *(double *)data = value;
            break;
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueFloat(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jfloat value) {
    setFieldValue<jfloat, setValueFloat>(env, sqlda, index, value);
}

inline void setValueDouble(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jdouble value) {
    switch (code) {
        case SQL_D_FLOAT:
        case SQL_DOUBLE:
            *(double *)data = value;
            break;
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueDouble(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jdouble value) {
    setFieldValue<jdouble, setValueDouble>(env, sqlda, index, value);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueInt128(JNIEnv *env, jclass clazz, jlong sqlda, jint index,
                                              jlong a, jlong b) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    if (handle != nullptr) {
        auto p = *handle;
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            if (v->sqlind != nullptr)
                *v->sqlind = 0;
            switch (v->sqltype & ~1) {
                case SQL_INT128: {
                    auto data = (FB_I128 *)v->sqldata;
                    data->fb_data[0] = a;
                    data->fb_data[1] = b;
                    break;
                }
                default:
                    throwDataConversionError(env, index);
            }
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);
}

inline void setValueDate(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jint value) {
    switch (code) {
        case SQL_TYPE_DATE:
        case SQL_TIMESTAMP:
        case SQL_TIMESTAMP_TZ:
        case SQL_TIMESTAMP_TZ_EX:
            *(ISC_DATE *)data = value + 40587;
            break;
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueDate(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jint value) {
    setFieldValue<jint, setValueDate>(env, sqlda, index, value);
}

inline void setValueTime(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jint value) {
    switch (code) {
        case SQL_TYPE_TIME:
        case SQL_TIME_TZ:
        case SQL_TIME_TZ_EX:
            *(ISC_TIME*) data = value * 10;
            break;
        case SQL_TIMESTAMP:
        case SQL_TIMESTAMP_TZ:
        case SQL_TIMESTAMP_TZ_EX:
            (*(ISC_TIMESTAMP *)data).timestamp_time = value * 10;
            break;
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueTime(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jint value) {
    setFieldValue<jint, setValueTime>(env, sqlda, index, value);
}

inline void setValueTimeZone(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jint value) {
    if (value >= 0 && value <= 65535) {
        auto tz = (ISC_USHORT)value;
        switch (code) {
            case SQL_TIME_TZ:
            case SQL_TIME_TZ_EX:
                ((ISC_TIME_TZ *) data)->time_zone = tz;
                break;
            case SQL_TIMESTAMP_TZ:
            case SQL_TIMESTAMP_TZ_EX:
                ((ISC_TIMESTAMP_TZ *) data)->time_zone = tz;
                break;
            default:
                throwDataConversionError(env, index);
        }
    } else
        throwDataConversionError(env, index);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueTimeZone(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jint value) {
    setFieldValue<jint, setValueTimeZone>(env, sqlda, index, value);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_execute(JNIEnv *env, jclass clazz, jlong status, jlong tr_handle, jlong st_handle, jshort dialect, jlong sqlda) {
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    auto stHandle = reinterpret_cast<FB_API_HANDLE*>(st_handle);
    auto xsqlda   = reinterpret_cast<const XSQLDA **>(sqlda);
    const auto da = xsqlda != nullptr?*xsqlda: nullptr;
    return dsql_execute(statusArray, trHandle, stHandle, dialect, da);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_execute2(JNIEnv *env, jclass clazz, jlong status, jlong tr_handle, jlong st_handle, jshort dialect, jlong input, jlong output) {
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto trHandle = reinterpret_cast<FB_API_HANDLE*>(tr_handle);
    auto stHandle = reinterpret_cast<FB_API_HANDLE*>(st_handle);
    auto i = reinterpret_cast<const XSQLDA **>(input);
    auto o = reinterpret_cast<const XSQLDA **>(output);
    const auto dai = i != nullptr?*i: nullptr;
    const auto dao = o != nullptr?*o: nullptr;
    return dsql_execute2(statusArray, trHandle, stHandle, dialect, dai, dao);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_fetch(JNIEnv *env, jclass clazz, jlong status, jlong st_handle, jlong sqlda) {
    const auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto stHandle = reinterpret_cast<FB_API_HANDLE*>(st_handle);
    auto xsqlda   = reinterpret_cast<const XSQLDA **>(sqlda);
    const auto da = xsqlda != nullptr?*xsqlda: nullptr;
    return dsql_fetch(statusArray,  stHandle, SQLDA_VERSION1, da);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_progdigy_fbclient_API_getIsNull(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    auto handle = reinterpret_cast<XSQLDA **>(sqlda);
    if (handle != nullptr) {
        auto p = *handle;
        if (index >= 0 && index < p->sqld) {
            auto v = &p->sqlvar[index];
            if (v->sqlind != nullptr)
                return *v->sqlind!=0?JNI_TRUE:JNI_FALSE;
            return JNI_FALSE;
        } else
            throwOutOfBoundError(env, index);
    } else
        throwHandleError(env);

    return JNI_FALSE;
}

inline jboolean getValueBoolean(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_BOOLEAN:
            return *(char *) data != FB_FALSE;
        default:
            throwDataConversionError(env, index);
            return JNI_FALSE;
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_progdigy_fbclient_API_getValueBoolean(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jboolean, getValueBoolean>(env, sqlda, index);
}

inline jshort getValueShort(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_SHORT:
            return *(ISC_SHORT *) data;
        default:
            throwDataConversionError(env, index);
            return 0;
    }
}

extern "C"
JNIEXPORT jshort JNICALL
Java_com_progdigy_fbclient_API_getValueShort(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jshort, getValueShort>(env, sqlda, index);
}

inline jint getValueInt(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_LONG:
            return *(ISC_LONG *) data;
        case SQL_SHORT:
            return *(ISC_SHORT *) data;
        default:
            throwDataConversionError(env, index);
            return 0;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_getValueInt(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jint, getValueInt>(env, sqlda, index);
}

inline jlong getValueLong(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_INT64:
        case SQL_QUAD:
            return *(ISC_INT64 *) data;
        case SQL_LONG:
            return *(ISC_LONG *) data;
        case SQL_SHORT:
            return *(ISC_SHORT *) data;
        default:
            throwDataConversionError(env, index);
            return 0;
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_getValueLong(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jlong, getValueLong>(env, sqlda, index);
}

inline jstring getValueString(JNIEnv* env, ISC_STATUS* status, FB_API_HANDLE* dbHandle, FB_API_HANDLE* trHandle,
                              int index, ISC_SCHAR* data, ISC_SHORT code, ISC_SHORT len, ISC_SHORT subtype) {
    switch (code) {
        case SQL_VARYING: {
            if (subtype == 4) {
                auto vary = (PARAMVARY*)data;
                vary->vary_string[vary->vary_length] = 0;
                return env->NewStringUTF((char*)&vary->vary_string);
            }
            break;
        }
        case SQL_TEXT: {
            if (subtype == 4) {
                auto size = utf8_size(data, len / subtype, len);
                data[size] = 0;
                return env->NewStringUTF(data);
            }
            break;
        }
        case SQL_BLOB:
            if (subtype == 1) {
                isc_blob_handle blob = 0;
                char* str = nullptr;
                auto ret = open_blob(status, dbHandle, trHandle, &blob, (GDS_QUAD*)data);
                if (ret == 0) {
                    char buffer[9];
                    char info = isc_info_blob_total_length;
                    ret = blob_info(status, &blob, 1, &info, sizeof buffer, &buffer);
                    if (ret == 0) {
                        auto length = (int)*(ISC_ULONG*)&buffer[3];
                        if (length >= 0) {
                            str = (char*)malloc(length + 1);
                            str[length] = 0;
                            if (length > 0) {
                                auto toRead = std::min(length, (int)std::numeric_limits<ISC_SHORT>::max());
                                ISC_USHORT size = 0;
                                auto p = str;
                                ret = get_segment(status, &blob, &size, toRead, p);
                                while (ret == 0 || status[1] == isc_segment) {
                                    length -= size;
                                    if (length <= 0)
                                        break;
                                    toRead = std::min(length, (int)std::numeric_limits<ISC_SHORT>::max());
                                    p += size;
                                    ret = get_segment(status, &blob, &size, toRead, p);
                                }
                            }
                        }
                    }
                    ret = close_blob(status, &blob);
                    if (str != nullptr) {
                        auto jstr = env->NewStringUTF(str);
                        free(str);
                        return jstr;
                    }
                }
                checkStatus(env, status, ret);
            }
            break;
        default:
            break;
    }

    throwDataConversionError(env, index);
    return nullptr;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_progdigy_fbclient_API_getValueString(JNIEnv *env, jclass clazz, jlong status, jlong db_handle, jlong tr_handle, jlong sqlda, jint index) {
    return getFieldValue<jstring, getValueString>(env, status, db_handle, tr_handle, sqlda, index);
}

inline jbyteArray getValueByteArray(JNIEnv* env, ISC_STATUS* status, FB_API_HANDLE* dbHandle, FB_API_HANDLE* trHandle,
                                    int index, ISC_SCHAR* data, ISC_SHORT code, ISC_SHORT len, ISC_SHORT subtype) {
    switch (code) {
        case SQL_VARYING: {
            auto vary = (PARAMVARY*)data;
            auto arr = env->NewByteArray(vary->vary_length);
            auto bytes = env->GetByteArrayElements(arr, nullptr);
            memcpy(bytes, vary->vary_string, vary->vary_length);
            env->ReleaseByteArrayElements(arr, bytes, JNI_COMMIT);
            return arr;
        }
        case SQL_TEXT: {
            auto arr = env->NewByteArray(len);
            auto bytes = env->GetByteArrayElements(arr, nullptr);
            memcpy(bytes, data, len);
            env->ReleaseByteArrayElements(arr, bytes, JNI_COMMIT);
            return arr;
        }
        case SQL_BLOB: {
            isc_blob_handle blob = 0;
            jbyteArray arr = nullptr;
            auto ret = open_blob(status, dbHandle, trHandle, &blob, (GDS_QUAD*)data);
            if (ret == 0) {
                char buffer[9];
                char info = isc_info_blob_total_length;
                ret = blob_info(status, &blob, 1, &info, sizeof buffer, &buffer);
                if (ret == 0) {
                    auto length = (int)*(ISC_ULONG*)&buffer[3];
                    arr = env->NewByteArray(length);
                    if (length > 0) {
                        auto bytes = env->GetByteArrayElements(arr, nullptr);
                        auto toRead = std::min(length, (int)std::numeric_limits<ISC_SHORT>::max());
                        ISC_USHORT size = 0;
                        auto p = bytes;
                        ret = get_segment(status, &blob, &size, toRead, p);
                        while (ret == 0 || status[1] == isc_segment) {
                            length -= size;
                            if (length <= 0)
                                break;
                            toRead = std::min(length, (int)std::numeric_limits<ISC_SHORT>::max());
                            p += size;
                            ret = get_segment(status, &blob, &size, toRead, p);
                        }
                        env->ReleaseByteArrayElements(arr, bytes, JNI_COMMIT);
                    }
                }
                ret = close_blob(status, &blob);
            }
            checkStatus(env, status, ret);
            return arr;
        }
        default:
            throwDataConversionError(env, index);
            return nullptr;
    }
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_progdigy_fbclient_API_getValueByteArray(JNIEnv *env, jclass clazz, jlong status, jlong db_handle, jlong tr_handle, jlong sqlda, jint index) {
    return getFieldValue<jbyteArray, getValueByteArray>(env, status, db_handle, tr_handle, sqlda, index);
}

inline jfloat getValueFloat(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    if (code == SQL_FLOAT)
        return *(float *) data;
    else {
        throwDataConversionError(env, index);
        return 0;
    }
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_progdigy_fbclient_API_getValueFloat(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jfloat, getValueFloat>(env, sqlda, index);
}

inline jdouble getValueDouble(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_FLOAT:
            return *(float *) data;
        case SQL_DOUBLE:
        case SQL_D_FLOAT:
            return *(double *) data;
        default:
            throwDataConversionError(env, index);
            return 0;
    }
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_progdigy_fbclient_API_getValueDouble(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jdouble, getValueDouble>(env, sqlda, index);
}

inline jlongArray getValueInt128(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_SHORT: {
            auto arr = env->NewLongArray(2);
            auto i = env->GetLongArrayElements(arr, nullptr);
            auto int16 =  *(ISC_SHORT *)data;
            i[0] = int16;
            i[1] =  (int16>=0)?0:-1;
            env->ReleaseLongArrayElements(arr, i, JNI_COMMIT);
            return arr;
        }
        case SQL_LONG: {
            auto arr = env->NewLongArray(2);
            auto i = env->GetLongArrayElements(arr, nullptr);
            auto int32 =  *(ISC_LONG *)data;
            i[0] = int32;
            i[1] =  (int32>=0)?0:-1;
            env->ReleaseLongArrayElements(arr, i, JNI_COMMIT);
            return arr;
        }
        case SQL_QUAD:
        case SQL_INT64: {
            auto arr = env->NewLongArray(2);
            auto i = env->GetLongArrayElements(arr, nullptr);
            auto int64 =  *(ISC_INT64*)data;
            i[0] = int64;
            i[1] =  (int64>=0)?0:-1;
            env->ReleaseLongArrayElements(arr, i, JNI_COMMIT);
            return arr;
        }
        case SQL_INT128: {
            auto arr = env->NewLongArray(2);
            auto i = env->GetLongArrayElements(arr, nullptr);
            memcpy(i, data, sizeof (FB_I128));
            env->ReleaseLongArrayElements(arr, i, JNI_COMMIT);
            return arr;
        }
        default:
            throwDataConversionError(env, index);
            return nullptr;
    }
}

extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_progdigy_fbclient_API_getValueInt128(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jlongArray, getValueInt128>(env, sqlda, index);
}

inline jint getValueDate(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_TYPE_DATE:
        case SQL_TIMESTAMP:
        case SQL_TIMESTAMP_TZ:
        case SQL_TIMESTAMP_TZ_EX:
            //LocalDate(1858, 11, 17).toEpochDays() = -40587
            return *(ISC_DATE *)data - 40587;
        default:
            throwDataConversionError(env, index);
            return 0;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_getValueDate(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jint, getValueDate>(env, sqlda, index);
}

inline jint getValueTime(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_TYPE_TIME:
        case SQL_TIME_TZ:
        case SQL_TIME_TZ_EX:
            return (jint)(*(ISC_TIME *)data / 10);
        case SQL_TIMESTAMP:
        case SQL_TIMESTAMP_TZ:
        case SQL_TIMESTAMP_TZ_EX:
            return (jint)(((ISC_TIMESTAMP *)data)->timestamp_time / 10);
        default:
            throwDataConversionError(env, index);
            return 0;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_getValueTime(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jint, getValueTime>(env, sqlda, index);
}

inline jint getValueTimeZone(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_TIME_TZ:
        case SQL_TIME_TZ_EX:
            return ((ISC_TIME_TZ *)data)->time_zone;
        case SQL_TIMESTAMP_TZ:
        case SQL_TIMESTAMP_TZ_EX:
            return ((ISC_TIMESTAMP_TZ *)data)->time_zone;
        default:
            throwDataConversionError(env, index);
            return 0;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_getValueTimeZone(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jint, getValueTimeZone>(env, sqlda, index);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_blobOpen(JNIEnv *env, jclass clazz, jlong status, jlong db_handle, jlong tr_handle,
                                        jlong blob_handle, jlong blob_id) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE *>(db_handle);
    auto trHandle = reinterpret_cast<FB_API_HANDLE *>(tr_handle);
    auto blobHandle = reinterpret_cast<FB_API_HANDLE *>(blob_handle);
    return open_blob(statusArray, dbHandle, trHandle, blobHandle, (GDS_QUAD*)&blob_id);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_blobClose(JNIEnv *env, jclass clazz, jlong status,
                                         jlong blob_handle) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto blobHandle = reinterpret_cast<FB_API_HANDLE *>(blob_handle);
    auto ret = close_blob(statusArray, blobHandle);
    *blobHandle = 0;
    return ret;
}

inline void setValueBlobId(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code, jlong value) {
    switch (code) {
        case SQL_BLOB:
            *(jlong *)data = value;
            break;
        default:
            throwDataConversionError(env, index);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_progdigy_fbclient_API_setValueBlobId(JNIEnv *env, jclass clazz, jlong sqlda, jint index, jlong value) {
    setFieldValue<jlong, setValueBlobId>(env, sqlda, index, value);
}

inline jlong getValueBlobId(JNIEnv* env, int index, ISC_SCHAR* data, ISC_SHORT code) {
    switch (code) {
        case SQL_BLOB:
            return *((jlong *)data);
        default:
            throwDataConversionError(env, index);
            return 0;
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_getValueBlobId(JNIEnv *env, jclass clazz, jlong sqlda, jint index) {
    return getFieldValue<jlong, getValueBlobId>(env, sqlda, index);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_blobRead(JNIEnv *env, jclass clazz, jlong status, jlong blob_handle, jbyteArray buffer, jint offset, jint length) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto blobHandle = reinterpret_cast<FB_API_HANDLE *>(blob_handle);
    auto arrayLength = env->GetArrayLength(buffer);
    int total = 0;
    if (arrayLength > 0 && offset >= 0 && length > 0) {
        arrayLength = std::min(arrayLength - offset, length);
        auto arr = env->GetByteArrayElements(buffer, nullptr);

        auto p = &arr[offset];
        ISC_USHORT size = 0;

        while (arrayLength > 0) {
            auto toRead = std::min(arrayLength, (jsize)std::numeric_limits<ISC_SHORT>::max());
            auto ret = get_segment(statusArray, blobHandle, &size, toRead, p);
            auto success = ret == 0 || statusArray[1] == isc_segment;
            while (success && size == 0) {
                ret = get_segment(statusArray, blobHandle, &size, toRead, p);
                success = ret == 0 || statusArray[1] == isc_segment;
            }
            if (size > 0) {
                p += size;
                arrayLength -= size;
                total += size;
                size = 0;
            } else
                break;
        }
        env->ReleaseByteArrayElements(buffer, arr, JNI_COMMIT);
    }
    return total;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_blobLength(JNIEnv *env, jclass clazz, jlong status, jlong blob_handle) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto blobHandle = reinterpret_cast<FB_API_HANDLE*>(blob_handle);
    char buffer[9];
    char info = isc_info_blob_total_length;
    auto ret = blob_info(statusArray, blobHandle, 1, &info, sizeof buffer, &buffer);
    if (ret == 0)
        return *(ISC_ULONG*)&buffer[3];
    else
        return 0;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_progdigy_fbclient_API_blobCreate(JNIEnv *env, jclass clazz, jlong status, jlong db_handle, jlong tr_handle,
                                        jlong blob_handle) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto dbHandle = reinterpret_cast<FB_API_HANDLE *>(db_handle);
    auto trHandle = reinterpret_cast<FB_API_HANDLE *>(tr_handle);
    auto blobHandle = reinterpret_cast<FB_API_HANDLE *>(blob_handle);
    jlong blobId = 0;
    checkStatus(env, status, create_blob(statusArray, dbHandle, trHandle, blobHandle, (GDS_QUAD*)&blobId));
    return blobId;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_progdigy_fbclient_API_blobWrite(JNIEnv *env, jclass clazz, jlong status, jlong blob_handle, jbyteArray buffer, jint offset, jint length) {
    auto statusArray = reinterpret_cast<ISC_STATUS*>(status);
    auto blobHandle = reinterpret_cast<FB_API_HANDLE *>(blob_handle);
    auto arrayLength = env->GetArrayLength(buffer);
    int total = 0;
    if (arrayLength > 0 && offset >= 0 && length > 0) {
        arrayLength = std::min(arrayLength - offset, length);
        auto arr = env->GetByteArrayElements(buffer, nullptr);
        auto p = &arr[offset];
        while (arrayLength > 0) {
            auto toWrite = std::min(arrayLength, (jsize)std::numeric_limits<ISC_SHORT>::max());
            if (toWrite > 0 && put_segment(statusArray, blobHandle, toWrite, p) == 0) {
                p += toWrite;
                arrayLength -= toWrite;
                total += toWrite;
            } else
                break;
        }
        env->ReleaseByteArrayElements(buffer, arr, JNI_COMMIT);
    }
    return total;
}

