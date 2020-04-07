package com.vespa.baek.cafeoma.inventory.view.presenter;

public interface InventoryContract {

    interface View{
        //show 등 완전 출력하는 부분위주
        //[InventoryActivity]






        //[ModifyInventoryActivity]

        //이름은 필수입력. 이름이 null이면 에러표시
        void showError();

        //넘어온 정보가 있으면 미리 MI액티비티에 뿌려주는
        void initData();

    }
    interface Presenter {

        //[InventoryActivity]

        //Inventory 아이템 클릭시 view에서 받은 정보를 넣어놓는다.
        void getData();

        //수정버튼을 눌러온건지 저장버튼을 눌러온건지 구별해서 Item정보를 저장해놓음


        //[ModifyInventoryActivity]



        //MI 이름을 입력했는지 확인
        void isNameNull();

    }

}
