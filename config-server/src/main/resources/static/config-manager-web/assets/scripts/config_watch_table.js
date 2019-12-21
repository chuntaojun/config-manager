$(function(){

    $.ajax({
        url: '/api/v1/namespace/all',
        type: 'GET',
        headers: {
            "Content-Type": 'application/json;charset=utf-8',
            "config-manager-token": sessionStorage.getItem("access_token")
        },
        contentType: 'application/json;charset=utf-8',
        success: function(response) {
            let item;
            if (response['code'] === 200) {
                const namespaces = response['data']
                let html = "";
                for (let i = 0; i < namespaces.length; i++) {
                    item = namespaces[i]
                    html += "<option value='" + item['namespaceId'] + "'>" + item['namespaceId'] + "</option>"
                }
                $('#select-item-namespace').append(html)
            } else {
                alert(response['errMsg'])
                window.location.href = '/'
            }
        }
    })

    $('#config_watch_table').bootstrapTable({
        classes: 'table table-hover',
        height: undefined,
        undefinedText: '-',
        sortName: undefined,
        sortOrder: 'asc',
        striped: true,
        editable: true,
        columns: [{
            checkbox: true
        }, {
            field: 'id',
            title: '编号',
        }, {
            field: 'clientId',
            title: '客户端标识',
        }, {
            field: 'address',
            title: '客户端IP',
        }, {
            field: 'lastMd5',
            title: '最新配置签名',
        }, {
            field: 'watchType',
            title: '监听方式'
        }],
        data:[{
            'id': '-',
            'clientId': '-',
            'address': '-',
            'lastMd5': '-',
            'watchType': '-'
        }],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/v1/config/watchClient',
        cache: false,
        contentType: 'application/json',
        ajaxOptions:{
            headers: {"config-manager-token": sessionStorage.getItem("access_token")}
        },
        queryParams: function (params) {
            params['namespaceId'] = $('#select-item-namespace').val()
            params['groupId'] = $('#item-groupId').val()
            params['dataId'] = $('#item-dataId').val()
            return params;
        },
        pagination: true,
        sidePagination: 'client', // client or server
        pageNumber: 1,
        pageSize: 10,
        pageList: [10, 25],
        search: false,
        selectItemName: 'btSelectItem',
        showHeader: true,
        showColumns: false,
        showRefresh: true,
        showToggle: true,
        smartDisplay: false,
        minimumCountColumns: 1,
        uniqued: 'id',
        idField: 'id',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar_seller',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
    })
})
