//全局变量
var setting = {    
	check: {
		enable: true
	},
    data: {
        simpleData: {
            enable: true
        }       
    }, 
    callback: {
        onClick: onClick,
        onCheck:onCheck
    }
};

//节点的点击事件
function onClick(event, treeId, treeNode) {
	console.log(treeNode.name);
};	

//复选框选中事件
	
function onCheck(event, treeId, treeNode){
	console.log($.fn.zTree.getZTreeObj(treeId).getCheckedNodes(true));
	//console.log(treeNodes);
}

//初始化树
function getTreeData () {
	$.ajax({
		url:'../t11_initfoldertree/getTreeNode',
		asyn:false,
		method:'post',  
		dataType:'json',  
		success:function(data){
			for(var i=0;i<data.length;i++){
				data[i]['open']=true;
			}
			$.fn.zTree.init($("#treeDemo"), setting, data);
		},
		error:function(){
			console.log("error");
		}
	});
};
$(function(){
	getTreeData();
});