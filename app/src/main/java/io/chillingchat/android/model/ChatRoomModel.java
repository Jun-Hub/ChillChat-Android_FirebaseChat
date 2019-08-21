package io.chillingchat.android.model;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.chillingchat.android.model.dto.ChatRoom;
import io.chillingchat.android.mvp_interface.ChatRoomMVP;

//TODO 네트워크 연결상태를 고려하여, success면 보여주고 fail이면 '네트워크 ㅄ이므로 다시 시도' 문구 보여주기. / 여기뿐아니라 대부분 데이터로드에 적용될듯.

public class ChatRoomModel implements ChatRoomMVP.Model {

    private ChatRoomMVP.Presenter chatRoomPresenter;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private ValueEventListener addListener;

    public ChatRoomModel(ChatRoomMVP.Presenter presenter) {
        this.chatRoomPresenter = presenter;
    }

    @Override
    public void addChatRoomList() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(addListener != null) return;

        reference = FirebaseDatabase.getInstance().getReference("ChatRooms");
        addListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                chatRoomPresenter.removeAll();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);

                    if (chatRoom != null) {
                        ChatRoom.Info info = chatRoom.getInfo();

                        if (info != null && info.getUser1Uid().equals(firebaseUser.getUid())) {    //현재 나와 채팅하고 있는 채팅방이라면
                            chatRoomPresenter.addChatRoom(chatRoom);
                        } else if (info != null && info.getUser2Uid().equals(firebaseUser.getUid())) {
                            chatRoomPresenter.addChatRoom(chatRoom);
                        }
                    }
                }
                chatRoomPresenter.getRoomCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void removeListener() {
        if(addListener != null) {
            reference.removeEventListener(addListener);
        }
        addListener = null;
    }
}
