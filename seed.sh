#!/usr/bin/env bash
# 더미데이터 추가 스크립트

set -euo pipefail

DATA_DIR="${DATA_DIR:-data}"
TS="$(date '+%Y-%m-%d %H:%M:%S')"

POST_TOTAL="${POST_TOTAL:-31}"                    # 게시글 개수
POST1_COMMENT_TOTAL="${POST1_COMMENT_TOTAL:-31}"  # 1번 글 댓글 개수

mkdir -p "$DATA_DIR"

# image : id,fileName,createdAt
cat > "$DATA_DIR/images.csv" <<EOF
1,sample1.png,$TS
2,sample2.png,$TS
3,post1.png,$TS
4,post2.png,$TS
EOF

# user : id,email,password,nickname,profileImageUrl
cat > "$DATA_DIR/users.csv" <<EOF
1,user1@test.com,Test1234!,user1,/images/sample1.png
2,user2@test.com,Test1234!,user2,/images/sample2.png
3,user3@test.com,Test1234!,user3,
EOF

# post : id,userId,title,content,viewCount,createdAt,postImageUrl
cat > "$DATA_DIR/posts.csv" <<EOF
1,1,게시글 제목 1,게시글 내용 1,0,$TS,/images/post1.png
2,1,게시글 제목 2,게시글 내용 2,5,$TS,
3,2,게시글 제목 3,게시글 내용 3,10,$TS,/images/post2.png
4,3,게시글 제목 4,게시글 내용 4,3,$TS,
EOF

for ((i = 5; i <= POST_TOTAL; i++)); do
  userId=$(( (i - 1) % 3 + 1 ))
  printf '%s,%s,게시글 제목 %s,게시글 내용 %s,%s,%s,\n' \
    "$i" "$userId" "$i" "$i" "$i" "$TS" >> "$DATA_DIR/posts.csv"
done

# comment : id,postId,userId,content,createdAt
cat > "$DATA_DIR/comments.csv" <<EOF
1,1,2,1번 글 댓글 1,$TS
2,1,3,1번 글 댓글 2,$TS
3,3,1,3번 글 댓글 1,$TS
4,4,2,4번 글 댓글 1,$TS
EOF

# 1번 글의 댓글 POST1_COMMENT_TOTAL개까지 채우기
comment_id=5
post1_comments=2
for ((c = post1_comments + 1; c <= POST1_COMMENT_TOTAL; c++)); do
  userId=$(( (comment_id - 1) % 3 + 1 ))
  printf '%s,1,%s,1번 글 댓글 %s,%s\n' \
    "$comment_id" "$userId" "$c" "$TS" >> "$DATA_DIR/comments.csv"
  comment_id=$((comment_id + 1))
done

echo "== 더미데이터 추가 완료 =="
echo "   images:   $(wc -l < "$DATA_DIR/images.csv") rows"
echo "   users:    $(wc -l < "$DATA_DIR/users.csv") rows"
echo "   posts:    $(wc -l < "$DATA_DIR/posts.csv") rows"
echo "   comments: $(wc -l < "$DATA_DIR/comments.csv") rows"