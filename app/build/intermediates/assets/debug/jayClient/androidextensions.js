/**
 * Created with IntelliJ IDEA.
 * User: Brandon
 * Date: 4/13/15
 * Time: 10:08 PM
 * To change this template use File | Settings | File Templates.
 */

var AndroidExtensions = {
    reviewData: [],

    getBestNodes: function(){
        Jay.nodeScan(function(){
            MyInterface.getBestNodesResult(JSON.stringify(Jay.bestNodes));
        });
    },
    onRequestSuccess: function(data){
        MyInterface.onRequestSuccess(data);
    },
    onRequestFailed: function(){
        MyInterface.onRequestFailed();
    }
};
