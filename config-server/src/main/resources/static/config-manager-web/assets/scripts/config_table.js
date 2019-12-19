function requireBtn(value, row, index) {
    return [
        '<button class="LookDetail btn btn-info" data-toggle="modal" data-target="#add-item" style="margin-right:15px;">可访问资源</button>'
    ].join('');
}

window.operateEvents = {
    'click .LookDetail': function (e, value, row, index) {
        const namespaceId = ''
        const groupId = row['groupId']
        const dataId = row['dataId']
        $.ajax({
            url: '/api/v1/config/detail?namespaceId=&groupId=&dataId=',
            type: 'GET',
            headers: {
                "Content-Type": 'application/json;charset=utf-8',
                "config-manager-token": sessionStorage.getItem("access_token")
            },
            contentType: 'application/json;charset=utf-8',
            success: function(response) {
                if (response['code'] === 200) {
                    $('#item-name').val(row[''])
                    $('#item-group').val(row['groupId'])
                    $('#item-data').val(row['dataId'])
                    $('#item-config-type').val(row[''])
                    $('#item-remark').val(row[''])
                    $('#item-select_beta').val(row[''])
                    $('#beta_client_ids').val(row[''])
                    $('#item-content').val(row[''])
                } else {
                    alert(response['errMsg'])
                }
            }
        })
    },
}

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

    $('#config_table').bootstrapTable({
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
            field: 'groupId',
            title: 'GroupId',
        }, {
            field: 'dataId',
            title: 'DataId',
        }, {
            field: 'type',
            title: 'ConfigType',
        }, {
            field: 'details',
            title: '详细',
            events: operateEvents,
            formatter: requireBtn,
        }],
        data:[],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/v1/config/list',
        cache: true,
        contentType: 'application/json',
        ajaxOptions:{
            headers: {"config-manager-token": sessionStorage.getItem("access_token")}
        },
        queryParams: function (params) {
            const selectItem = document.getElementById("select-item-namespace");
            const namespaceId = selectItem.options[selectItem.selectedIndex].value;
            params['namespaceId'] = namespaceId
            params['groupId'] = $('#groupId_input').val()
            params['dataId'] = $('#dataId_input').val()
            console.log(JSON.stringify(params))
            return params;
        },
        pagination: true,
        sidePagination: 'server', // client or server
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
