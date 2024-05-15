/*
    Cinterpop requires a modified version of the Firebird API, as it wrongly interprets buffers as strings
    that should not be transliterated.
*/


#ifndef FIREBIRD_FBCLIENT_H
#define FIREBIRD_FBCLIENT_H

#include "./firebird/impl/types_pub.h"
#include "./firebird/impl/sqlda_pub.h"
#include "./firebird/impl/inf_pub.h"
#include "./iberror.h"

/******************************************************************/
/* Status vector                                                  */
/******************************************************************/

typedef intptr_t ISC_STATUS;

#define ISC_STATUS_LENGTH	20
typedef ISC_STATUS ISC_STATUS_ARRAY[ISC_STATUS_LENGTH];

/* SQL State as defined in the SQL Standard. */
#define FB_SQLSTATE_LENGTH	5
#define FB_SQLSTATE_SIZE	(FB_SQLSTATE_LENGTH + 1)
typedef char FB_SQLSTATE_STRING[FB_SQLSTATE_SIZE];

#if !defined(JRD_VAL_H)
/* This is a helper struct to work with varchars. */
typedef struct paramvary {
    ISC_USHORT		vary_length;
    ISC_UCHAR		vary_string[1];
} PARAMVARY;
#endif /* !defined(JRD_VAL_H) */

#ifdef __cplusplus
extern "C" {
#endif

ISC_STATUS ISC_EXPORT isc_attach_database(ISC_STATUS *,
                                          short,
                                          const void *,
                                          isc_db_handle *,
                                          short,
                                          const void *);

ISC_STATUS ISC_EXPORT isc_create_database(ISC_STATUS*,
                                          unsigned short,
                                          const void*,
                                          isc_db_handle*,
                                          unsigned short,
                                          const void*,
                                          unsigned short);

ISC_STATUS ISC_EXPORT isc_detach_database(ISC_STATUS *,
                                          isc_db_handle *);

ISC_STATUS ISC_EXPORT isc_dsql_execute_immediate(ISC_STATUS*,
                                                 isc_db_handle*,
                                                 isc_tr_handle*,
                                                 unsigned short,
                                                 const void*,
                                                 unsigned short,
                                                 const XSQLDA*);

/* This const params version used in the engine and other places. */
ISC_LONG ISC_EXPORT fb_interpret(ISC_SCHAR*,
                                 unsigned int,
                                 const ISC_STATUS**);

ISC_STATUS ISC_EXPORT_VARARG isc_start_transaction(ISC_STATUS*,
                                                   isc_tr_handle*,
                                                   short, ...);

ISC_STATUS ISC_EXPORT isc_commit_retaining(ISC_STATUS *,
                                           isc_tr_handle *);

ISC_STATUS ISC_EXPORT isc_commit_transaction(ISC_STATUS *,
                                             isc_tr_handle *);

ISC_STATUS ISC_EXPORT isc_rollback_retaining(ISC_STATUS*,
                                             isc_tr_handle*);

ISC_STATUS ISC_EXPORT isc_rollback_transaction(ISC_STATUS*,
                                               isc_tr_handle*);

ISC_STATUS ISC_EXPORT isc_dsql_allocate_statement(ISC_STATUS *,
                                                  isc_db_handle *,
                                                  isc_stmt_handle *);

ISC_STATUS ISC_EXPORT isc_dsql_prepare(ISC_STATUS*,
                                       isc_tr_handle*,
                                       isc_stmt_handle*,
                                       unsigned short,
                                       const void*,
                                       unsigned short,
                                       XSQLDA*);

ISC_STATUS ISC_EXPORT isc_dsql_set_cursor_name(ISC_STATUS*,
                                               isc_stmt_handle*,
                                               const ISC_SCHAR*,
                                               unsigned short);

ISC_STATUS ISC_EXPORT isc_dsql_describe(ISC_STATUS *,
                                        isc_stmt_handle *,
                                        unsigned short,
                                        XSQLDA *);

ISC_STATUS ISC_EXPORT isc_dsql_describe_bind(ISC_STATUS *,
                                             isc_stmt_handle *,
                                             unsigned short,
                                             XSQLDA *);

ISC_STATUS ISC_EXPORT isc_dsql_execute(ISC_STATUS*,
                                       isc_tr_handle*,
                                       isc_stmt_handle*,
                                       unsigned short,
                                       const XSQLDA*);

ISC_STATUS ISC_EXPORT isc_dsql_execute2(ISC_STATUS*,
                                        isc_tr_handle*,
                                        isc_stmt_handle*,
                                        unsigned short,
                                        const XSQLDA*,
                                        const XSQLDA*);

ISC_STATUS ISC_EXPORT isc_dsql_fetch(ISC_STATUS *,
                                     isc_stmt_handle *,
                                     unsigned short,
                                     const XSQLDA *);

ISC_STATUS ISC_EXPORT isc_dsql_free_statement(ISC_STATUS *,
                                              isc_stmt_handle *,
                                              unsigned short);

ISC_STATUS ISC_EXPORT isc_open_blob(ISC_STATUS*,
                                    isc_db_handle*,
                                    isc_tr_handle*,
                                    isc_blob_handle*,
                                    ISC_QUAD*);

ISC_STATUS ISC_EXPORT isc_get_segment(ISC_STATUS *,
                                      isc_blob_handle *,
                                      unsigned short *,
                                      unsigned short,
                                      void *);

ISC_STATUS ISC_EXPORT isc_put_segment(ISC_STATUS*,
                                      isc_blob_handle*,
                                      unsigned short,
                                      const void*);


ISC_STATUS ISC_EXPORT isc_blob_info(ISC_STATUS*,
                                    isc_blob_handle*,
                                    short,
                                    const void*,
                                    short,
                                    void*);

ISC_STATUS ISC_EXPORT isc_close_blob(ISC_STATUS *,
                                     isc_blob_handle *);

ISC_STATUS ISC_EXPORT isc_create_blob(ISC_STATUS*,
                                      isc_db_handle*,
                                      isc_tr_handle*,
                                      isc_blob_handle*,
                                      ISC_QUAD*);

#ifdef __cplusplus
}
#endif

#endif //FIREBIRD_FBCLIENT_H